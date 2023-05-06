import java.io.IOException
import java.util.{StringTokenizer, TimeZone, UUID}
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
import java.time.{LocalDate, ZoneId}

case class TimeDataRaw(date: String)
object TimeDataRaw {
  implicit val decoder: JsonDecoder[TimeDataRaw] = DeriveJsonDecoder.gen[TimeDataRaw]
  implicit val encoder: JsonEncoder[TimeDataRaw] = DeriveJsonEncoder.gen[TimeDataRaw]
}

case class TimeData(date: LocalDate)
object TimeData {
  implicit val decoder: JsonDecoder[TimeData] = DeriveJsonDecoder.gen[TimeData]
  implicit val encoder: JsonEncoder[TimeData] = DeriveJsonEncoder.gen[TimeData]

  def fromRaw(raw: TimeDataRaw) = {
    TimeData(LocalDate.parse(raw.date))
  }
}

case class PlayCount(timestamp: Long, count: Long)
object PlayCount {
  implicit val decoder: JsonDecoder[PlayCount] = DeriveJsonDecoder.gen[PlayCount]
  implicit val encoder: JsonEncoder[PlayCount] = DeriveJsonEncoder.gen[PlayCount]
}

case class SteamInput(game_id: Int, playcounts: List[PlayCount])
object SteamInput {
  implicit val decoder: JsonDecoder[SteamInput] = DeriveJsonDecoder.gen[SteamInput]
  implicit val encoder: JsonEncoder[SteamInput] = DeriveJsonEncoder.gen[SteamInput]
}

case class MapperResult(gameId: Int, playCount: Long)
object MapperResult {
  implicit val decoder: JsonDecoder[MapperResult] = DeriveJsonDecoder.gen[MapperResult]
  implicit val encoder: JsonEncoder[MapperResult] = DeriveJsonEncoder.gen[MapperResult]
}

case class ReduceResult(date: LocalDate, mostPopular: Long, averageGamersCount: Long, total: Long)
object ReduceResult {
  implicit val decoder: JsonDecoder[ReduceResult] = DeriveJsonDecoder.gen[ReduceResult]
  implicit val encoder: JsonEncoder[ReduceResult] = DeriveJsonEncoder.gen[ReduceResult]
}

object Main {
  class MyMapper extends HadoopJob.HadoopMapper[AnyRef, Text, Text, Text] {
    // If the input is date => (date, "include")
    // If the input is steam => (date, MapperResult)
    override def myMap(key: AnyRef, value: Text, emit: (Text, Text) => Unit): Unit = {
      def handleTimeData(input: TimeDataRaw) = {
        val parsed = TimeData.fromRaw(input)
        emit(Text(parsed.date.toJson), new Text("include"))
      }

      def handleSteamData(input: SteamInput) = {
        val byDay = input.playcounts.map(x => {
          (java.time.Instant.ofEpochMilli(x.timestamp).atZone(ZoneId.of("UTC")).toLocalDate, input.game_id, x.count)
        })

        byDay.foreach(x => {
          val day       = x._1
          val gameId    = x._2
          val playCount = x._3
          emit(new Text(day.toJson), new Text(MapperResult(gameId, playCount).toJson))
        })
      }

      if (value.toString.fromJson[TimeDataRaw].toOption.isDefined) {
        handleTimeData(value.toString.fromJson[TimeDataRaw].toOption.get)
      }

      if (value.toString.fromJson[SteamInput].toOption.isDefined) {
        handleSteamData(value.toString.fromJson[SteamInput].toOption.get)
      }
    }
  }

  class MyReducer extends HadoopJob.HadoopReducer[Text, Text, Text] {
    // If the values contains "include" => calculate
    // If the values do not contain "include" => do nothing
    override def myReduce(key: Text, values: List[String], emit: (Text, Text) => Unit): Unit = {
      val doInclude = values.contains("include")
      if (!doInclude) return

      val date       = key.toString.fromJson[LocalDate].toOption.get
      val playCounts = values.filterNot(_ == "include").map(x => x.fromJson[MapperResult].toOption.get)

      emit(
        Text("id"),
        Text(
          ReduceResult(
            date = date,
            mostPopular = playCounts.maxBy(_.playCount).gameId,
            averageGamersCount = playCounts.map(_.playCount).sum / playCounts.length,
            total = playCounts.map(_.playCount).sum
          ).toJson
        )
      )
    }
  }

  def main(args: Array[String]) = {
    val conf = new Configuration
    val job  = Job.getInstance(conf, "steam04")

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
