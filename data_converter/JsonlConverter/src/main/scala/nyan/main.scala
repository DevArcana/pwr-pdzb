package nyan

import com.github.tototoshi.csv.CSVWriter
import nyan.StoreConverter
import nyan.StoreConverter
import zio.*
import zio.stream.*

import java.io.{File, StringWriter}

object Main extends ZIOAppDefault {
  val store = for {
    args <- getArgs
    store = StoreConverter.Convert(args.head)
    csvs = StoreConverter.ToCsv(store)
    sink = ZSink.fromFile(File(args(1)))
    kok = csvs.flatMap(e => ZStream.from(e.getBytes(java.nio.charset.StandardCharsets.UTF_8).toList))
    _ <- kok >>> sink
  }
  yield()

  val spy = for {
    args <- getArgs
    store = SpyConverter.Convert(args(2))
    csvs = SpyConverter.ToCsv(store)
    sink = ZSink.fromFile(File(args(3)))
    kok = csvs.flatMap(e => ZStream.from(e.getBytes(java.nio.charset.StandardCharsets.UTF_8).toList))
    _ <- kok >>> sink
  }
  yield ()

  private val myApp =
    for {
      storeFiber <- store.fork
      spyFiber <- spy.fork
      _ <- storeFiber.join
      _ <- spyFiber.join

    } yield ()

  override def run = myApp
}