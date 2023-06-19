from pyspark.sql import SparkSession

data = "/examples/data.jsonl"
spark = SparkSession.builder.appName("App").getOrCreate()

df = spark.read.json(data)
df.select("name").write.csv('/spark-result/dataframe-select', header=True)

spark.read.json(data).createOrReplaceTempView("table")
spark.sql("SELECT name FROM table").write.csv('/spark-result/sql-select', header=True)

spark.stop()