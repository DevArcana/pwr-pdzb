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

case class CategorySnippet(
    title: String
)

object CategorySnippet {
  implicit val decoder: JsonDecoder[CategorySnippet] = DeriveJsonDecoder.gen[CategorySnippet]
  implicit val encoder: JsonEncoder[CategorySnippet] = DeriveJsonEncoder.gen[CategorySnippet]
}

case class Category(
    id: String,
    snippet: CategorySnippet
)

object Category {
  implicit val decoder: JsonDecoder[Category] = DeriveJsonDecoder.gen[Category]
  implicit val encoder: JsonEncoder[Category] = DeriveJsonEncoder.gen[Category]
}

case class InputCategories(
    key: String,
    value: List[Category]
)

object InputCategories {
  implicit val decoder: JsonDecoder[InputCategories] = DeriveJsonDecoder.gen[InputCategories]
  implicit val encoder: JsonEncoder[InputCategories] = DeriveJsonEncoder.gen[InputCategories]
}

case class InputVideos(
    video_id: String,
    title: String,
    publishedAt: String,
    channelId: String,
    channelTitle: String,
    categoryId: String,
    trending_date: String,
    tags: String,
    view_count: String,
    likes: String,
    dislikes: String,
    comment_count: String,
    thumbnail_link: String,
    comments_disabled: String,
    ratings_disabled: String,
    description: String
)

object InputVideos {
  implicit val decoder: JsonDecoder[InputVideos] = DeriveJsonDecoder.gen[InputVideos]
  implicit val encoder: JsonEncoder[InputVideos] = DeriveJsonEncoder.gen[InputVideos]
}

case class Output(
    video_id: String,
    title: String,
    category_id: Int,
    category_name: String,
    comment_count: Int,
    view_count: Int,
    trending_date: String
)

object Output {
  implicit val decoder: JsonDecoder[Output] = DeriveJsonDecoder.gen[Output]
  implicit val encoder: JsonEncoder[Output] = DeriveJsonEncoder.gen[Output]
}

object Main {
  class MyMapper extends HadoopJob.HadoopMapper[AnyRef, Text, Text, Text] {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override def myMap(key: AnyRef, value: Text, emit: (Text, Text) => Unit): Unit = {
      val text = value.toString

      if text.startsWith("{\"key\":") then
        text
          .split("\n")
          .map(x => x.dropWhile(!_.isWhitespace))
          .flatMap(x => InputCategories.decoder.decodeJson(x).toOption)
          .filter(x => x.key == "items")
          .flatMap(x => x.value)
          .foreach(x => {
            emit(Text(x.id), Text(x.toJson))
          })
      else
        text
          .split("\n")
          .map(x => x.dropWhile(!_.isWhitespace))
          .flatMap(x => InputVideos.decoder.decodeJson(x).toOption)
          .foreach(x => {
            emit(Text(x.categoryId), Text(x.toJson))
          })
    }
  }

  class MyReducer extends HadoopJob.HadoopReducer[Text, Text, Text] {
    override def myReduce(key: Text, values: List[String], emit: (Text, Text) => Unit): Unit = {
      val categoryFilter = (x: String) => x.contains(s"\"key\": \"${key.toString}\"")

      val category = values
        .filter(categoryFilter)
        .flatMap(x => Category.decoder.decodeJson(x).toOption)
        .head

      values
        .filter(x => !categoryFilter(x))
        .flatMap(x => InputVideos.decoder.decodeJson(x).toOption)
        .map(x =>
          Output(
            x.video_id,
            x.title,
            category.id.toIntOption.getOrElse(-1),
            category.snippet.title,
            x.comment_count.toIntOption.getOrElse(0),
            x.view_count.toIntOption.getOrElse(0),
            x.trending_date
          )
        )
        .foreach(x => emit(Text("id"), Text(x.toJson)))
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

    job.setInputFormatClass(classOf[TextInputFormat])
    FileInputFormat.addInputPath(job, new Path(args(0)))
    FileOutputFormat.setOutputPath(job, new Path(args(1)))

    java.lang.System.exit(
      if (job.waitForCompletion(true)) 0
      else 1
    )
  }
}
