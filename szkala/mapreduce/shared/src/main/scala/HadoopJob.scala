import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.{Mapper, Reducer}
import zio.*

object HadoopJob {

  val runtime = Runtime.default

  trait HadoopMapper[KIn, VIn, KOut, VOut] extends Mapper[KIn, VIn, KOut, VOut] {

    def runZIO[E, A](f: ZIO[Any, E, A]) = Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe.run(f).getOrThrowFiberFailure()
    }

    def myMap(key: KIn, value: VIn, emit: (KOut, VOut) => Unit): Unit

    override def map(
        key: KIn,
        value: VIn,
        context: Mapper[KIn, VIn, KOut, VOut]#Context
    ): Unit = {
      myMap(key, value, (k, v) => context.write(k, v))
    }
  }

  trait HadoopReducer[KIn, KOut, VOut] extends Reducer[KIn, Text, KOut, VOut] {

    def runZIO[E, A](f: ZIO[Any, E, A]) = Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe.run(f).getOrThrowFiberFailure()
    }

    override def reduce(
        key: KIn,
        values: java.lang.Iterable[Text],
        context: Reducer[KIn, Text, KOut, VOut]#Context
    ): Unit = {
      import scala.jdk.CollectionConverters._
      val javaVals = Utils.convert(values)
      val vals     = javaVals.asScala.toList
      myReduce(key, vals, (k, v) => context.write(k, v))
    }

    def myReduce(key: KIn, values: List[String], emit: (KOut, VOut) => Unit): Unit

  }
}
