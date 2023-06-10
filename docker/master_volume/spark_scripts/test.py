from pyspark.sql import SparkSession

covid = "/datasets/covid-dataset.jsonl"
spark = SparkSession.builder.appName("CovidApp").getOrCreate()

df = spark.read.json(covid)
df.select("location").write.csv('/spark-result/dataframe-select', header=True)

spark.read.json(covid).createOrReplaceTempView("covid")
spark.sql("SELECT location FROM covid").write.csv('/spark-result/sql-select', header=True)

spark.stop()