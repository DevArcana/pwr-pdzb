from pyspark.sql import SparkSession
from pyspark.sql.functions import col, avg, when
from pyspark.sql.window import Window

covid = "/datasets/covid-dataset.jsonl"
spark = SparkSession.builder.appName("CovidApp").getOrCreate()

df = spark.read.json(covid)

result = df.select(
    col("date"),
    col("location"),
    when(col("total_cases") != "", col("total_cases").cast("int")).otherwise(0).alias("total_cases"),
    when(col("new_cases") != "", col("new_cases").cast("int")).otherwise(0).alias("new_cases"),
    when(col("total_deaths") != "", col("total_deaths").cast("int")).otherwise(0).alias("total_deaths"),
    when(col("new_deaths") != "", col("new_deaths").cast("int")).otherwise(0).alias("new_deaths"),
    when(col("new_cases_per_million") != "", col("new_cases_per_million").cast("int")).otherwise(0).alias("new_cases_per_million"),
    avg(when(col("new_cases_per_million") != "", col("new_cases_per_million").cast("int")).otherwise(0)).over(Window.orderBy()).alias("average_new_cases_per_million")
)

result.write.csv('/spark-result/covid/df', header=True)

spark.stop()
