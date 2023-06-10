from pyspark.sql import SparkSession

covid = "/datasets/covid-dataset.jsonl"
spark = SparkSession.builder.appName("CovidApp").getOrCreate()

df = spark.read.json(covid)

df.select("location").write.csv('/spark-result/foo.csv', header=True)

spark.stop()