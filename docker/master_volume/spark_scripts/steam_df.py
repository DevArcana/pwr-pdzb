from pyspark.sql import SparkSession
from pyspark.sql.functions import col

steam_store = "/datasets/steam-dataset/steam_dataset/appinfo/store_data/steam_store_data.jsonl"
steam_spy   = "/datasets/steam-dataset/steam_dataset/steamspy/basic/steam_spy_scrap.jsonl"

spark = SparkSession.builder.appName("SteamApp").getOrCreate()

store = spark.read.json(steam_store).select(
    col("value.steam_appid").alias("steam_appid"),
    col("value.release_date.coming_soon").alias("coming_soon"),
    col("value.release_date.date").alias("date")
)

spy = spark.read.json(steam_spy).select(
    col("value.appid").alias("appid"),
    col("value.name").alias("name"),
    col("value.positive").alias("positive"),
    col("value.negative").alias("negative"),
    col("value.owners").alias("owners"),
    col("value.ccu").alias("ccu")
)

store.join(spy, store["steam_appid"] == spy["appid"]).write.csv('/spark-result/steam/df', header=True)

spark.stop()