import zio.stream.{ZPipeline, ZStream}

import java.nio.file.Files
import java.nio.file.Paths;

object ConvertFiles {

  def steam_spy(): Unit = {
    def steam_spy_text =
      Files.readString(Paths.get("/home/legusie/Documents/GitHub/pwr-pdzb/datasets/steam_dataset/steamspy/basic/steam_spy_scrap.json"))

    val steam_spy_res =
      steam_spy_text
        .drop(1)
        .reverse
        .dropWhile(x => x == '}' || x.isWhitespace)
        .reverse
        .mkString
        .split("},")
        .toList
        .map(x => x.replaceAll("[\\t\\n\\r]+", " "))
        .map(x => x.dropWhile(_ != '{'))
        .map(x => s" $x } ")
        //.take(100)
        .mkString("\n")
    Files.writeString(Paths.get("steam_spy_basic.jsonl"), steam_spy_res);
  }

  def steam_data() = {
    val steam_data =
      Files.readString(Paths.get("/home/legusie/Documents/GitHub/pwr-pdzb/datasets/steam_dataset/appinfo/store_data/steam_store_data.json"))

    val res =
      steam_data
        .drop(1)
        .dropRight(1)
        .split("\n    \"")
        .filterNot(_.isEmpty)
        .map(x => x.replaceAll("[\\t\\n\\r]+", " "))
        .map(x => s"{ \"$x } ")
        //.take(20)
        .toList

    // println(steam_spy_res)
    Files.writeString(Paths.get("steam_data.jsonl"), res.mkString("\n"));
  }

  def main(args: Array[String]): Unit = {
    steam_spy()
    //steam_data()

  }

}
