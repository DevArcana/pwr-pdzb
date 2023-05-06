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

case class Result(game_id: Int, name: String)

object Result {
  implicit val decoder: JsonDecoder[Result] = DeriveJsonDecoder.gen[Result]
  implicit val encoder: JsonEncoder[Result] = DeriveJsonEncoder.gen[Result]
}

object Main {
  class MyMapper extends HadoopJob.HadoopMapper[AnyRef, Text, Text, Text] {
    override def myMap(key: AnyRef, value: Text, emit: (Text, Text) => Unit): Unit = {
      val jsons = value.toString.split("\n").map(x => x.dropWhile(!_.isWhitespace)).toList
      val mapped = jsons.flatMap(x => SteamJoined.decoder.decodeJson(x).toOption)
      //val filtered = mapped.filter(game => overwhelminglyPositive(game)).filter(game => game.ccu > 5000).filter(game => game.owners)
      val result = mapped.map(x => Result(x.game_id, x.name))
    }

    private def overwhelminglyPositive(steamJoined: SteamJoined): Boolean = (steamJoined.positive / (steamJoined.positive + steamJoined.negative)) > 0.9
    //private def process_owners(steamJoined: SteamJoined): (Int, Int) = steamJoined.owners.replace('.', ' ').split(' ')
  }

  class MyReducer extends HadoopJob.HadoopReducer[Text, Text, Text] {
    override def myReduce(key: Text, values: List[String], emit: (Text, Text) => Unit): Unit = {

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
