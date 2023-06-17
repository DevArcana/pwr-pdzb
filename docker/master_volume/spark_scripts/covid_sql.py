from pyspark.sql import SparkSession

covid = "/datasets/covid-dataset.jsonl"
spark = SparkSession.builder.appName("CovidApp").getOrCreate()

spark.read.json(covid).createOrReplaceTempView("covid")
spark.sql("""
WITH transformed_data AS (
SELECT
    date,
    location,
    CAST(total_cases AS INT) AS total_cases,
    CAST(new_cases AS INT) AS new_cases,
    CAST(total_deaths AS INT) AS total_deaths,
    CAST(new_deaths AS INT) AS new_deaths,
    CASE WHEN new_cases_per_million = '' THEN 0 ELSE CAST(new_cases_per_million AS INT) END AS new_cases_per_million
FROM covid
)
SELECT
    date,
    location,
    total_cases,
    new_cases,
    total_deaths,
    new_deaths,
    new_cases_per_million,
    avg(new_cases_per_million) OVER () AS average_new_cases_per_million
FROM transformed_data
""").write.csv('/spark-result/covid/sql', header=True)

spark.stop()