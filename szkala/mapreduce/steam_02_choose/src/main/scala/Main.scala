import java.io.IOException
import java.util.{Calendar, StringTokenizer, UUID}
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
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalField
import scala.util.{Failure, Success, Try}

case class SteamJoined(
  game_id: Int,
  name: String,
  positive: Int,
  negative: Int,
  owners: String,
  ccu: Int,
  release_date: String
)

object SteamJoined {
  implicit val decoder: JsonDecoder[SteamJoined] = DeriveJsonDecoder.gen[SteamJoined]
  implicit val encoder: JsonEncoder[SteamJoined] = DeriveJsonEncoder.gen[SteamJoined]
}

object Main {
  private val isoFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  class MyMapper extends HadoopJob.HadoopMapper[AnyRef, Text, Text, Text] {
    override def myMap(key: AnyRef, value: Text, emit: (Text, Text) => Unit): Unit = {

      value.toString.split("\n").map(x => x.dropWhile(!_.isWhitespace)).toList
        .flatMap(x => SteamJoined.decoder.decodeJson(x).toOption)
        .filter(game => before2020(game.release_date))
        .filter(game => overwhelminglyPositive(game))
        .filter(game => game.ccu > 5000)
        .filter(game => has_many_owners(game))
        .foreach(x => emit(Text(x.game_id.toString), Text(x.toJson)))


/*      value.toString.split("\n").map(x => x.dropWhile(!_.isWhitespace)).toList
        .flatMap(x => SteamJoined.decoder.decodeJson(x).toOption)
        .filter(game => before2020(game.release_date))
        .filter(game => game.positive > 30000)
        .filter(game => (game.positive.toDouble / (game.positive + game.negative)) > 0.9)
        .foreach(x => emit(Text("id"), Text(x.toJson)))*/
    }

    private def overwhelminglyPositive(steamJoined: SteamJoined): Boolean = steamJoined.positive > 30000 && (steamJoined.positive.toDouble / (steamJoined.positive + steamJoined.negative)) > 0.8
    private def before2020(date: String): Boolean = {
      Try(isoFormat.parse(date)) match
        case Failure(exception) => false
        case Success(value) => value.get(java.time.temporal.ChronoField.YEAR) < 2020
    }
//10,000,000 .. 20,000,000
    private def has_many_owners(steamJoined: SteamJoined): Boolean = Try(steamJoined.owners.replace(",", "").split('.').head.trim.toInt) match
      case Failure(exception) => false
      case Success(value) => value > 5000000
  }

  class MyReducer extends HadoopJob.HadoopReducer[Text, Text, Text] {
    override def myReduce(key: Text, values: List[String], emit: (Text, Text) => Unit): Unit = {
      values.foreach(x => emit(key, Text(x)))
    }
  }

  def main(args: Array[String]) = {
    val conf = new Configuration
    val job = Job.getInstance(conf, "Select games for further analysis")

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
