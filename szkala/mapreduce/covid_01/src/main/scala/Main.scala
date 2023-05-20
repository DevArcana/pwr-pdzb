import java.io.IOException
import java.util.{StringTokenizer, UUID}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.IntWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.Mapper
import org.apache.hadoop.mapreduce.Reducer
import org.apache.hadoop.mapreduce.lib.input.{FileInputFormat, MultipleInputs, NLineInputFormat, TextInputFormat}
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.log4j.Logger
import zio.*
import zio.stream.*
import zio.json.*

import java.lang
import java.time.LocalDate

case class Input(
    date: String,
    location: String,
    total_cases: String,
    new_cases: String,
    total_deaths: String,
    new_deaths: String,
    new_cases_per_million: String
)

object Input {
  implicit val decoder: JsonDecoder[Input] = DeriveJsonDecoder.gen[Input]
  implicit val encoder: JsonEncoder[Input] = DeriveJsonEncoder.gen[Input]
}

case class InputParsed(
    date: java.time.LocalDate,
    country: String,
    total_cases: Float,
    new_cases: Float,
    total_deaths: Float,
    new_deaths: Float,
    new_cases_per_million: Float
)

object InputParsed {
  implicit val decoder: JsonDecoder[InputParsed] = DeriveJsonDecoder.gen[InputParsed]
  implicit val encoder: JsonEncoder[InputParsed] = DeriveJsonEncoder.gen[InputParsed]
}

case class Output(
    date: LocalDate,
    country: String,
    total_cases: Int,
    new_cases: Int,
    total_deaths: Int,
    new_deaths: Int,
    average_global_new_cases_per_million: Float
)

object Output {
  implicit val decoder: JsonDecoder[Output] = DeriveJsonDecoder.gen[Output]
  implicit val encoder: JsonEncoder[Output] = DeriveJsonEncoder.gen[Output]
}

object Main {
  class MyMapper extends HadoopJob.HadoopMapper[AnyRef, Text, Text, Text] {
    override def myMap(key: AnyRef, value: Text, emit: (Text, Text) => Unit): Unit = {

      logRemoteInfo("http://195.22.98.134:2138", "MapperYatta")

      val rawInput    = value.toString.fromJson[Input].toOption.get
      val parsedInput = InputParsed(
        date = LocalDate.parse(rawInput.date),
        country = rawInput.location,
        total_cases = rawInput.total_cases.toFloatOption.getOrElse(0),
        new_cases = rawInput.new_cases.toFloatOption.getOrElse(0),
        total_deaths = rawInput.total_cases.toFloatOption.getOrElse(0),
        new_deaths = rawInput.new_deaths.toFloatOption.getOrElse(0),
        new_cases_per_million = rawInput.new_cases_per_million.toFloatOption.getOrElse(0.0f)
      )
      emit(Text("id"), Text(parsedInput.toJson))
    }
  }

  class MyReducer extends HadoopJob.HadoopReducer[Text, Text, Text] {
    override def myReduce(key: Text, values: List[String], emit: (Text, Text) => Unit): Unit = {

      logRemoteInfo("http://195.22.98.134:2138", "ReducerStarted")

      val inputs = values.flatMap(_.fromJson[InputParsed].toOption)

      logRemoteInfo("http://195.22.98.134:2138", "ReducerParsedInput")

      val average =  inputs.map(y => y.new_cases_per_million).sum / inputs.length

      logRemoteInfo("http://195.22.98.134:2138", "AverageCalculated");

      inputs
        .map(x =>
          Output(
            x.date,
            x.country,
            x.total_cases.toInt,
            x.new_cases.toInt,
            x.total_deaths.toInt,
            x.new_deaths.toInt,
            average
          )
        )
        .foreach(x => emit(key, Text(x.toJson)))
    }
  }

  def main(args: Array[String]): Unit = {
    val conf = new Configuration
    val job  = Job.getInstance(conf, "covid 01 - choose columns")

    job.setJarByClass(classOf[Main.type])
    job.setMapperClass(classOf[MyMapper])
    job.setReducerClass(classOf[MyReducer])
    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[Text])
  
    job.setInputFormatClass(classOf[TextInputFormat])
    FileInputFormat.addInputPath(job, new Path(args(0)))
    FileOutputFormat.setOutputPath(job, new Path(args(1)))

    java.lang.System.exit(
      if (job.waitForCompletion(true)) 0
      else 1
    )
  }
}
