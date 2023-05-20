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

case class Input(
    date: String,
    country: String,
    total_cases: Int,
    new_cases: Int,
    total_deaths: Int,
    new_deaths: Int,
    average_global_new_cases_per_million: Float
)

object Input {
  implicit val decoder: JsonDecoder[Input] = DeriveJsonDecoder.gen[Input]
  implicit val encoder: JsonEncoder[Input] = DeriveJsonEncoder.gen[Input]
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
        .flatMap(x => Input.decoder.decodeJson(x).toOption)
        .foreach(x => {
          val date = LocalDate.parse(x.date, formatter)

          for (n <- Range(0, 8)) {
            emit(Text(s"${x.country}/${date.plusDays(n).format(formatter)}"), Text(x.toJson))
          }
        })
    }
  }

  class MyReducer extends HadoopJob.HadoopReducer[Text, Text, Text] {
    override def myReduce(key: Text, values: List[String], emit: (Text, Text) => Unit): Unit = {
      val list = values
        .flatMap(x => Input.decoder.decodeJson(x).toOption)
        .sortBy(x => x.date)
        .reverse

      //emit(Text("debug"), Text(values.toString()))

      list match
        case head :: tail =>
          emit(
            key,
            Text(
              Output(
                head.date,
                head.country,
                head.total_cases,
                head.new_cases,
                head.total_deaths,
                head.new_deaths,
                (tail.map(x => x.new_cases).sum / tail.length.toFloat) / head.new_cases,
                head.average_global_new_cases_per_million
              ).toJson
            )
          )
        case Nil          => ()
    }
  }

  def main(args: Array[String]): Unit = {
    val conf = new Configuration
    val job  = Job.getInstance(conf, "covid 03 - calculate spread")

    job.setJarByClass(classOf[Main.type])
    job.setMapperClass(classOf[MyMapper])
    job.setReducerClass(classOf[MyReducer])
    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[Text])

    job.setNumReduceTasks(3)
    job.setInputFormatClass(classOf[TextInputFormat])
    FileInputFormat.addInputPath(job, new Path(args(0)))
    FileOutputFormat.setOutputPath(job, new Path(args(1)))

    java.lang.System.exit(
      if (job.waitForCompletion(true)) 0
      else 1
    )
  }
}
