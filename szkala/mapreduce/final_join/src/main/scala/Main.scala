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
import java.time.format.DateTimeFormatter

case class InputCovid(
                       date: String,
                       country: String,
                       total_cases: Int,
                       new_cases: Int,
                       total_deaths: Int,
                       new_deaths: Int,
                       covid_spread_speed: Float,
                       average_global_new_cases_per_million: Float
)

object InputCovid {
  implicit val decoder: JsonDecoder[InputCovid] = DeriveJsonDecoder.gen[InputCovid]
  implicit val encoder: JsonEncoder[InputCovid] = DeriveJsonEncoder.gen[InputCovid]
}

case class Output(
    date: String,
    country: String,
    total_cases: Int,
    new_cases: Int,
    total_deaths: Int,
    new_deaths: Int,
    covid_spread_speed: Float,
    average_global_new_cases_per_million: Float
)

object Output {
  implicit val decoder: JsonDecoder[Output] = DeriveJsonDecoder.gen[Output]
  implicit val encoder: JsonEncoder[Output] = DeriveJsonEncoder.gen[Output]
}

object Main {
  class MyMapper extends HadoopJob.HadoopMapper[AnyRef, Text, Text, Text] {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override def myMap(key: AnyRef, value: Text, emit: (Text, Text) => Unit): Unit = {
      value.toString
        .split("\n")
        .map(x => x.dropWhile(!_.isWhitespace))
        .flatMap(x => InputCovid.decoder.decodeJson(x).toOption)
        .foreach(x => emit(Text(x.date), Text(x.toJson)))
    }
  }

  class MyReducer extends HadoopJob.HadoopReducer[Text, Text, Text] {
    override def myReduce(key: Text, values: List[String], emit: (Text, Text) => Unit): Unit = {
      values
        .flatMap(x => InputCovid.decoder.decodeJson(x).toOption)
        .map(x => Output(
          x.date,
          x.country,
          x.total_cases,
          x.new_cases,
          x.total_deaths,
          x.new_deaths,
          x.covid_spread_speed,
          x.average_global_new_cases_per_million
        ))
        .foreach(x => emit(Text("id"), Text(x.toJson)))

    }
  }

  def main(args: Array[String]): Unit = {
    val conf = new Configuration
    val job  = Job.getInstance(conf, "final join by date")

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
