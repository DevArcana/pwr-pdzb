package nyan

import com.github.tototoshi.csv.CSVWriter
import com.github.tototoshi.csv.*
import steam.{ReleaseDate, SteamStore}
import zio.*
import zio.json.*
import zio.stream.*

import java.io.{File, StringWriter}
import java.nio.charset.Charset
import java.time.format.DateTimeFormatter
import scala.io.Source
import scala.util.{Failure, Success, Try}

object StoreConverter:
  private val format = DateTimeFormatter.ofPattern("MMM d, yyyy")

  def Convert(input: String) = {
    (ZStream.fromFile(File(input)) >>> ZPipeline.decodeCharsWith(java.nio.charset.StandardCharsets.UTF_8))
      .split(_=='\n')
      .map(_.mkString)
      .map(x => steam.SteamStoreBig.decoder.decodeJson(x).toOption).map(_.map(_.value)).filter(_.isDefined).map(_.get).filter(e => e.release_date match
      case ReleaseDate(true, _) => true
      case ReleaseDate(false, date) => Try(format.parse(date)) match
        case Failure(_) => false
        case Success(_) => true
    )
  }

  def ToCsv(storeObjects: ZStream[Any, Throwable, SteamStore]) = {
      storeObjects.map(e => ToCsvSingle(e)).map(e => {
        val writer = StringWriter()
        val writer2 = CSVWriter.open(writer)
        writer2.writeRow(e)
        writer2.close()
        val value = writer.toString
        writer.close()
        value
      })
  }

  private def ToCsvSingle(elem: SteamStore) = List(elem.steam_appid, elem.release_date.date, elem.release_date.coming_soon)

