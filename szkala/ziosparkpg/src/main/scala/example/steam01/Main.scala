package steam01

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.SaveMode

object Main {
  def main(args: Array[String]) {
    val spark = SparkSession.builder.appName("Steam01").getOrCreate()
    import spark.implicits._

    val steamStore = "/datasets/steam-dataset/steam_dataset/appinfo/store_data/steam_store_data.jsonl"
    val steamSpy   = "/datasets/steam-dataset/steam_dataset/steamspy/basic/steam_spy_scrap.jsonl"

    val steamSpyDf   = spark.read.json(steamSpy).as[model.SteamSpyRow]
    val steamStoreDf = spark.read.json(steamStore).as[model.SteamStoreRow]

    val result = steamSpyDf
      .joinWith(steamStoreDf, steamSpyDf("value.appid") === steamStoreDf("value.steam_appid"))
      .map(x => {
        val steamSpy   = x._1.value
        val steamStore = x._2.value

        model.SteamJoinedRow(
          x._1.key,
          model.SteamJoined(
            steamSpy.appid,
            steamSpy.name,
            steamSpy.positive,
            steamSpy.negative,
            steamSpy.owners,
            steamSpy.ccu,
            steamStore.release_date.date
          )
        )
      })

    result.write.mode(SaveMode.Overwrite).json("/spark-out/steam01")

    spark.stop()
  }
}
