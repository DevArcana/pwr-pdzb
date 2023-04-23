import model.{SteamSpyRow, SteamStoreRow}
import zio.{Chunk, ZIOAppDefault}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder, JsonStreamDelimiter}
import zio.stream.{ZPipeline, ZStream}
import zio.*

import java.nio.charset.Charset
import java.nio.file.Path

object TestMain extends ZIOAppDefault {
  val stream = ZStream.fromFile(Path.of("steam_spy_basic.jsonl").toFile) >>> ZPipeline.decodeCharsWith(Charset.defaultCharset())
  val mapped = stream >>> SteamSpyRow.decoder.decodeJsonPipeline(JsonStreamDelimiter.Newline)

  val stream2 = ZStream.fromFile(Path.of("steam_data.jsonl").toFile) >>> ZPipeline.decodeCharsWith(Charset.defaultCharset())
  val mapped2 = stream2 >>> SteamStoreRow.decoder.decodeJsonPipeline(JsonStreamDelimiter.Newline)

  override def run = for {
    //res <- mapped.runCollect
    //_   <- ZIO.foreach(res)(row => Console.printLine(row))

    res2 <- mapped2.runCollect
    _    <- ZIO.foreach(res2)(row => Console.printLine(row))
  } yield ()
}
