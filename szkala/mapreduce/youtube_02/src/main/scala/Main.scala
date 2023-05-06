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
import scala.util.{Failure, Success, Try}

case class InputVideo(
    video_id: String,
    title: String,
    category_id: Int,
    category_name: String,
    comment_count: Int,
    view_count: Int,
    trending_date: String
)

object InputVideo {
  implicit val decoder: JsonDecoder[InputVideo] = DeriveJsonDecoder.gen[InputVideo]
  implicit val encoder: JsonEncoder[InputVideo] = DeriveJsonEncoder.gen[InputVideo]
}

case class InputTime(
    date: String
)

object InputTime {
  implicit val decoder: JsonDecoder[InputTime] = DeriveJsonDecoder.gen[InputTime]
  implicit val encoder: JsonEncoder[InputTime] = DeriveJsonEncoder.gen[InputTime]
}

object Main {
  class MyMapper extends HadoopJob.HadoopMapper[AnyRef, Text, Text, Text] {
    override def myMap(key: AnyRef, value: Text, emit: (Text, Text) => Unit): Unit = {
      val text = value
        .toString
        .split("\n")
        .map(x => x.dropWhile(!_.isWhitespace))

      Try({
        text
          .flatMap(x => InputVideo.decoder.decodeJson(x).toOption)
          .foreach(x => {
            emit(Text("id"), Text(s"video/${x.toJson}"))
          })
      }) match
        case Failure(_) => text
          .flatMap(x => InputTime.decoder.decodeJson(x).toOption)
          .foreach(x => {
            emit(Text("id"), Text(s"time/${x.toJson}"))
          })
        case Success(_) => ()
    }
  }

  class MyReducer extends HadoopJob.HadoopReducer[Text, Text, Text] {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override def myReduce(key: Text, values: List[String], emit: (Text, Text) => Unit): Unit = {
      val time = values
        .filter(_.startsWith("time/"))
        .map(_.substring(5))
        .flatMap(_.fromJson[InputTime].toOption)
        .map(x => LocalDate.parse(x.date, formatter))

      values
        .filter(_.startsWith("video/"))
        .map(_.substring(6))
        .flatMap(_.fromJson[InputVideo].toOption)
        .filter(x => time.contains(LocalDate.parse(x.trending_date, DateTimeFormatter.ISO_DATE_TIME)))
        .foreach(x => emit(Text("id"), Text(x.toJson)))
    }
  }

  def main(args: Array[String]): Unit = {
    val conf = new Configuration
    val job  = Job.getInstance(conf, "youtube 02 - filter to time dimension")

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
