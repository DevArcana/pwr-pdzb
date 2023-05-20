package nyan

import com.github.tototoshi.csv.CSVWriter
import com.github.tototoshi.csv._
import steam.{ReleaseDate, SteamSpy, SteamStore}
import zio.stream.{ZPipeline, ZStream}

import java.io.{File, StringWriter}
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Success, Try}

object SpyConverter:
  private val format = DateTimeFormatter.ofPattern("MMM d, yyyy")

  def Convert(input: String) = {
    (ZStream.fromFile(File(input)) >>> ZPipeline.decodeCharsWith(java.nio.charset.StandardCharsets.UTF_8))
      .split(_=='\n')
      .map(_.mkString)
      .map(x => steam.SteamSpyBig.decoder.decodeJson(x).toOption).map(_.map(_.value)).filter(_.isDefined).map(_.get)
  }

  def ToCsv(spyObjects: ZStream[Any, Throwable, SteamSpy]) = {
    spyObjects.map(e => ToCsvSingle(e)).map(e => {
      val writer = StringWriter()
      val writer2 = CSVWriter.open(writer)
      writer2.writeRow(e)
      writer2.close()
      val value = writer.toString
      writer.close()
      value
    })
  }

  private def ToCsvSingle(elem: SteamSpy) = List(
    elem.appid,
    elem.name,
    elem.developer,
    elem.publisher,
    elem.score_rank,
    elem.positive,
    elem.negative,
    elem.userscore,
    elem.owners,
    elem.average_forever,
    elem.average_2weeks,
    elem.median_forever,
    elem.median_2weeks,
    elem.price.getOrElse(""),
    elem.initialprice.getOrElse(""),
    elem.discount.getOrElse(""),
    elem.ccu
  )

