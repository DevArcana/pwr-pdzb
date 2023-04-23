/*
import model.{SteamSpy, SteamSpyRow, SteamStore, SteamStoreRow}
import zio.{Chunk, ZIOAppDefault}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder, JsonStreamDelimiter}
import zio.stream.{ZPipeline, ZStream}
import zio.*

import java.nio.charset.Charset
import java.nio.file.Path

object TestMain extends ZIOAppDefault {
  val stream = ZStream.fromFile(Path.of("steam_spy_basic.jsonl").toFile) >>> ZPipeline.decodeCharsWith(Charset.defaultCharset())
  val mapped = stream >>> SteamSpyRow.decoder.decodeJsonPipeline(JsonStreamDelimiter.Newline)

  val stream2 = ZStream.fromFile(Path.of("steam_store_data.jsonl").toFile) >>> ZPipeline.decodeCharsWith(Charset.defaultCharset())
  val mapped2 = stream2 >>> SteamStoreRow.decoder.decodeJsonPipeline(JsonStreamDelimiter.Newline)

  override def run = for {
    _ <- ZIO.succeed({
      val x = SteamStore.decoder.decodeJson("""{"steam_appid":1000280,"release_date":{"coming_soon":false,"date":"Sep 9, 2021"}}""")
      val x2 = SteamSpy.decoder.decodeJson("""{"appid":1000000,"name":"ASCENXION","developer":"IndigoBlue Game Studio","publisher":"PsychoFlux Entertainment","score_rank":"","positive":27,"negative":5,"userscore":0,"owners":"0 .. 20,000","average_forever":0,"average_2weeks":0,"median_forever":0,"median_2weeks":0,"price":"999","initialprice":"999","discount":"0","ccu":0}""")
      println(x)
      println(x2)
    })
/*    res <- mapped.runCollect
    _   <- ZIO.foreach(res)(row => Console.printLine(row))

    res2 <- mapped2.runCollect
    _    <- ZIO.foreach(res2)(row => Console.printLine(row))*/
  } yield ()
}
*/
