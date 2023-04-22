import java.io.IOException
import java.util.StringTokenizer
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.IntWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.Mapper
import org.apache.hadoop.mapreduce.Reducer
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
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

case class ScoreRank(raw: String) {}

object ScoreRank {
  implicit val decoder: JsonDecoder[ScoreRank] = JsonDecoder[String].orElse(JsonDecoder[Int].map(_.toString)).map(x => ScoreRank(x))
}

case class SteamSpyRow(
    appid: Int,
    name: String,
    developer: String,
    publisher: String,
    score_rank: ScoreRank,
    positive: Int,
    negative: Int,
    userscore: Int,
    owners: String,
    average_forever: Int,
    average_2weeks: Int,
    median_forever: Int,
    median_2weeks: Int,
    price: Option[String],
    initialprice: Option[String],
    discount: Option[String],
    ccu: Int
) {}
object SteamSpyRow {
  implicit val decoder: JsonDecoder[SteamSpyRow] = DeriveJsonDecoder.gen[SteamSpyRow]
  //implicit val encoder: JsonEncoder[SteamSpyRow] = DeriveJsonEncoder.gen[SteamSpyRow]
}

object WordCount {
  object TokenizerMapper { private val one = new IntWritable(1) }

  private val logger = Logger.getLogger(classOf[Nothing])

  class TokenizerMapper extends Mapper[AnyRef, Text, Text, IntWritable]       {
    private val word = new Text

    override def map(key: AnyRef, value: Text, context: Mapper[AnyRef, Text, Text, IntWritable]#Context) = {
      val itr = new StringTokenizer(value.toString)

      val stream = ZStream.from(value.toString.toList)
      val mapped = stream >>> SteamSpyRow.decoder.decodeJsonPipeline(JsonStreamDelimiter.Newline)

      val a = runZIO(for {
        result <- mapped.runCollect
      } yield result)

      a.foreach(x => {
        context.write(Text(x.appid.toString), TokenizerMapper.one)
      })
    }
  }
  class IntSumReducer   extends Reducer[Text, IntWritable, Text, IntWritable] {
    private val result = new IntWritable

    override def reduce(key: Text, values: lang.Iterable[IntWritable], context: Reducer[Text, IntWritable, Text, IntWritable]#Context) = {
      import collection.convert.ImplicitConversionsToScala._

      val a = runZIO(
        ZStream.from(values.toList).map(x => x.get()).runSum
      )

      result.set(a)
      context.write(key, result)
    }
  }

  def main(args: Array[String]) = {
    val conf = new Configuration
    val job  = Job.getInstance(conf, "word count")

    job.setJarByClass(classOf[WordCount.type])
    job.setMapperClass(classOf[WordCount.TokenizerMapper])
    job.setCombinerClass(classOf[WordCount.IntSumReducer])
    job.setReducerClass(classOf[WordCount.IntSumReducer])
    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[IntWritable])

    // job.setInputFormatClass(classOf[NLineInputFormat])
    FileInputFormat.addInputPath(job, new Path(args(0)))
    // job.getConfiguration.setInt("mapreduce.input.lineinputformat.linespermap", 1)

    FileOutputFormat.setOutputPath(job, new Path(args(1)))
    java.lang.System.exit(
      if (job.waitForCompletion(true)) 0
      else 1
    )
  }
}
