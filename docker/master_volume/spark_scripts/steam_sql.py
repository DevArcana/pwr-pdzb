from pyspark.sql import SparkSession
from pyspark.sql.functions import col

steam = "/datasets/steam-dataset/steam_dataset/appinfo/store_data/steam_store_data.jsonl"
spark = SparkSession.builder.appName("SteamApp").getOrCreate()

spark.read.json(steam).select(
    col("value.type").alias("type"),
    col("value.name").alias("type"),
    col("value.steam_appid").alias("type"),
    col("value.type").alias("type"),
    col("value.type").alias("type")
).createOrReplaceTempView("steam")
spark.sql("""
SELECT * FROM steam
""").write.csv('/spark-result/steam/sql', header=True)

spark.stop()