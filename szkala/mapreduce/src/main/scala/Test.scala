import zio.{Chunk, ZIOAppDefault}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder, JsonStreamDelimiter}
import zio.stream.{ZPipeline, ZStream}
import zio._
import java.nio.charset.Charset
import java.nio.file.Path

object TestMain extends ZIOAppDefault {
  val stream       = (ZStream.fromFile(Path.of("steam_spy_basic.jsonl").toFile) >>> ZPipeline.decodeCharsWith(Charset.defaultCharset()))
  val mapped       = stream >>> SteamSpyRow.decoder.decodeJsonPipeline(JsonStreamDelimiter.Newline)
  override def run = for {
    res <- mapped.runCollect
    _   <- ZIO.foreach(res)(row => Console.printLine(row))
  } yield ()
}
