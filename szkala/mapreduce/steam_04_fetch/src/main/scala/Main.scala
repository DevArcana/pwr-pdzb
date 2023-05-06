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
import zio.{ZIO, *}
import zio.http.netty.ChannelFactories.Client
import zio.stream.*
import zio.json.*

import java.lang

case class Input(game_id: Int, name: String)
object Input {
  implicit val decoder: JsonDecoder[Input] = DeriveJsonDecoder.gen[Input]
  implicit val encoder: JsonEncoder[Input] = DeriveJsonEncoder.gen[Input]
}

type ApiResult = List[List[Long]]

case class PlayCount(timestamp: Long, count: Long)
object PlayCount {
  implicit val decoder: JsonDecoder[PlayCount] = DeriveJsonDecoder.gen[PlayCount]
  implicit val encoder: JsonEncoder[PlayCount] = DeriveJsonEncoder.gen[PlayCount]
}

case class Result(game_id: Int, playcounts: List[PlayCount])
object Result {
  implicit val decoder: JsonDecoder[Result] = DeriveJsonDecoder.gen[Result]
  implicit val encoder: JsonEncoder[Result] = DeriveJsonEncoder.gen[Result]
}

object Main {
  class MyMapper extends HadoopJob.HadoopMapper[AnyRef, Text, Text, Text] {
    override def myMap(key: AnyRef, value: Text, emit: (Text, Text) => Unit): Unit = {
      val jsons  = value.toString.split("\n").map(x => x.dropWhile(!_.isWhitespace)).map(_.trim).toList
      val mapped = jsons.flatMap(x => Input.decoder.decodeJson(x).toOption)

      def downloadTimestamps(id: Int) = for {
        res  <- zio.http.Client.request(f"""https://steamcharts.com/app/$id/chart-data.json""")
        data <- res.body.asString
        json  = data.fromJson[ApiResult].getOrElse(List.empty)
      } yield json

      val workflow = ZIO.foreach(mapped)(x => downloadTimestamps(x.game_id).map(res => (x, res)))

      val result = runZIO(
        workflow
          .tapError(err => { ZIO.succeed(emit(Text("error!"), Text(err.getMessage))) })
          .orElse(ZIO.succeed(List.empty))
          .provide(zio.http.Client.default)
      ).map(x => Result(x._1.game_id, x._2.map(y => PlayCount(y.head, y.last))))

      result.foreach { x =>
        emit(new Text(x.game_id.toString), Text(x.toJson))
      }
    }
  }

  class MyReducer extends HadoopJob.HadoopReducer[Text, Text, Text] {
    override def myReduce(key: Text, values: List[String], emit: (Text, Text) => Unit): Unit = {
      values.foreach(x => emit(key, Text(x)))
    }
  }

  def main(args: Array[String]) = {

    java.lang.System.setProperty("java.net.preferIPv4Stack", "true")

    val conf = new Configuration
    val job  = Job.getInstance(conf, "word count")

    job.setJarByClass(classOf[Main.type])
    job.setMapperClass(classOf[MyMapper])
    job.setReducerClass(classOf[MyReducer])
    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[Text])

    FileInputFormat.addInputPath(job, new Path(args(0)))
    FileOutputFormat.setOutputPath(job, new Path(args(1)))

    java.lang.System.exit(
      if (job.waitForCompletion(true)) 0
      else 1
    )
  }
}
