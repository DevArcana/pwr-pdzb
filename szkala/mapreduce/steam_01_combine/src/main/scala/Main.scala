import model.{SteamJoined, SteamJoinedRow, SteamSpy, SteamSpyRow, SteamStore, SteamStoreRow}

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

val runtime = Runtime.default;

def runZIO[E, A](f: ZIO[Any, E, A]) = Unsafe.unsafe { implicit unsafe =>
  runtime.unsafe.run(f).getOrThrowFiberFailure()
}

/*"570": {
  "appid": 570,
  "name": "Dota 2",
  "developer": "Valve",
  "publisher": "Valve",
  "score_rank": "",
  "positive": 1456697,
  "negative": 291673,
  "userscore": 0,
  "owners": "100,000,000 .. 200,000,000",
  "average_forever": 39576,
  "average_2weeks": 1788,
  "median_forever": 1021,
  "median_2weeks": 944,
  "price": "0",
  "initialprice": "0",
  "discount": "0",
  "ccu": 612293
}*/

object JoinSteamDatasets {
  private val logger = Logger.getLogger(classOf[Nothing])

  class SteamStoreOrSpyMapper extends Mapper[AnyRef, Text, Text, Text] {
    override def map(key: AnyRef, value: Text, context: Mapper[AnyRef, Text, Text, Text]#Context) = {
      val steamSpyStream   = ZStream.from(value.toString.toList) >>> SteamSpyRow.decoder.decodeJsonPipeline(JsonStreamDelimiter.Newline)
      val steamStoreStream = ZStream.from(value.toString.toList) >>> SteamStoreRow.decoder.decodeJsonPipeline(JsonStreamDelimiter.Newline)

      val steamSpyRows   = runZIO(steamSpyStream.take(10).runCollect.orElse(ZIO.succeed(Chunk.empty)))
      val steamStoreRows = runZIO(steamStoreStream.take(10).runCollect.orElse(ZIO.succeed(Chunk.empty)))

      steamSpyRows
        .foreach(x => {
          context.write(Text(x.value.appid.toString), Text(SteamSpy.encoder.encodeJson(x.value).toString))
        })

      steamStoreRows
        .foreach(x => {
          context.write(Text(x.value.steam_appid.toString), Text(SteamStore.encoder.encodeJson(x.value).toString))
        })
    }
  }

  class ReduceSteamDatasets extends Reducer[Text, Text, Text, Text] {
    override def reduce(key: Text, values: lang.Iterable[Text], context: Reducer[Text, Text, Text, Text]#Context): Unit = {
      // import collection.convert.ImplicitConversionsToScala._
      import scala.jdk.CollectionConverters._
      import scala.jdk.javaapi.FunctionConverters._

      val javaVals = Utils.convert(values)
      val vals     = javaVals.asScala.toList

      /*      context.write(
        Text(UUID.randomUUID().toString),
        Text(
          s"""
            | key: ${key.toString}
            | valsSize: ${vals.size}
            | vals: ${vals.toString}
            |""".stripMargin
        )
      )*/

      // There are max 2 rows per key
      // Something might be missing though
      val steamSpyRowOpt  = vals.map(_.toString).flatMap(x => SteamSpy.decoder.decodeJson(x).toOption).headOption
      val steamDataRowOpt = vals.map(_.toString).flatMap(x => SteamStore.decoder.decodeJson(x).toOption).headOption

      if (steamSpyRowOpt.isEmpty || steamDataRowOpt.isEmpty) {
        logger.error(s"Missing data for key: ${key.toString}")
        return
      }

      val steamSpyRow  = steamSpyRowOpt.get
      val steamDataRow = steamDataRowOpt.get

      val result = SteamJoined(
        steamSpyRow.appid,
        steamSpyRow.name,
        steamSpyRow.positive,
        steamSpyRow.negative,
        steamSpyRow.owners,
        steamSpyRow.ccu,
        steamDataRow.release_date.date
      )

      context.write(key, Text(SteamJoined.encoder.encodeJson(result).toString))
    }
  }

  def main(args: Array[String]) = {
    val conf = new Configuration
    val job  = Job.getInstance(conf, "word count")

    job.setJarByClass(classOf[JoinSteamDatasets.type])
    job.setMapperClass(classOf[JoinSteamDatasets.SteamStoreOrSpyMapper])
    job.setReducerClass(classOf[JoinSteamDatasets.ReduceSteamDatasets])
    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[Text])

    val steamSpyPath   = new Path(args(0))
    val steamStorePath = new Path(args(1))
    // FileInputFormat.addInputPaths(job, new Path(args(0)))
    MultipleInputs.addInputPath(job, steamSpyPath, classOf[TextInputFormat], classOf[SteamStoreOrSpyMapper])
    MultipleInputs.addInputPath(job, steamStorePath, classOf[TextInputFormat], classOf[SteamStoreOrSpyMapper])

    FileOutputFormat.setOutputPath(job, new Path(args(2)))

    java.lang.System.exit(
      if (job.waitForCompletion(true)) 0
      else 1
    )
  }
}
