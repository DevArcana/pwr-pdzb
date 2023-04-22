import java.io.IOException
import java.util.StringTokenizer
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.IntWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.Mapper
import org.apache.hadoop.mapreduce.Reducer
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import zio._
import zio.stream._

import java.lang

val runtime = Runtime.default;

def runZIO[E, A](f: ZIO[Any, E, A]) = Unsafe.unsafe { implicit unsafe =>
  runtime.unsafe.run(f).getOrThrowFiberFailure()
}

object WordCount {
  object TokenizerMapper { private val one = new IntWritable(1) }

  class TokenizerMapper extends Mapper[AnyRef, Text, Text, IntWritable] {
    private val word = new Text

    override def map(
        key: AnyRef,
        value: Text,
        context: Mapper[AnyRef, Text, Text, IntWritable]#Context
    ) = {
      val itr = new StringTokenizer(value.toString)
      while (itr.hasMoreTokens) {
        word.set(itr.nextToken)
        context.write(word, TokenizerMapper.one)
      }
    }
  }
  class IntSumReducer extends Reducer[Text, IntWritable, Text, IntWritable] {
    private val result = new IntWritable

    override def reduce(
        key: Text,
        values: lang.Iterable[IntWritable],
        context: Reducer[Text, IntWritable, Text, IntWritable]#Context
    ) = {
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
    val job = Job.getInstance(conf, "word count")
    job.setJarByClass(classOf[WordCount.type])
    job.setMapperClass(classOf[WordCount.TokenizerMapper])
    job.setCombinerClass(classOf[WordCount.IntSumReducer])
    job.setReducerClass(classOf[WordCount.IntSumReducer])
    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[IntWritable])
    FileInputFormat.addInputPath(job, new Path(args(0)))
    FileOutputFormat.setOutputPath(job, new Path(args(1)))
    java.lang.System.exit(
      if (job.waitForCompletion(true)) 0
      else 1
    )
  }
}
