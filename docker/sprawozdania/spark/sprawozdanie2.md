# Sprawozdanie 11

**Grupa A3:**

inż. Michał Liss

inż. Marceli Sokólski

inż. Piotr Krzystanek

## SQL vs Dataframe

### SQL
```python
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
```
### Dataframe
```python
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
```

|     | covid   | steam   |
| --- | ------- | ------- |
| df  | 13758.7 | 13911.9 |
| sql | 13749.5 | 13907.9 |


|           | run 0 | run 1 | run 2 | run 3 | run 4 | run 5 | run 6 | run 7 | run 8 | run 9 |
| --------- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- |
| steam df  | 13859 | 13817 | 13227 | 14443 | 14377 | 14525 | 13436 | 13964 | 13672 | 13799 |
| steam sql | 14267 | 14052 | 13415 | 13748 | 13712 | 13854 | 13764 | 13811 | 14529 | 13927 |
| covid df  | 14344 | 13400 | 13447 | 14340 | 13724 | 13262 | 13354 | 14398 | 13666 | 13652 |
| covid sql | 13758 | 14356 | 13586 | 13635 | 14173 | 13840 | 13625 | 13180 | 14037 | 13305 |

## Python vs Scala

### Python

```python
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
```
### Scala

```scala
object Main {
  def main(args: Array[String]) {
    val spark = SparkSession.builder.appName("Covid01").getOrCreate()
    import spark.implicits._

    val covid   = "/datasets/covid-dataset.jsonl"
    val covidDf = spark.read.json(covid).as[model.Row]

    val avg = covidDf.map(x => x.new_cases_per_million.toDoubleOption.getOrElse(0.0)).reduce(_ + _) / covidDf.count()

    val result = covidDf.map(x =>
      model.ResultRow(
        x.date,
        x.location,
        x.total_cases,
        x.new_cases,
        x.total_deaths,
        x.new_deaths,
        x.new_cases_per_million,
        avg
      )
    )

    result.write.json("/spark-out/covid")

    spark.stop()
  }
}
```

|        | covid   | steam   |
| ------ | ------- | ------- |
| python | 13758.7 | 13911.9 |
| scala  | 15889.8 | 15607.8 |

|              | run 0 | run 1 | run 2 | run 3 | run 4 | run 5 | run 6 | run 7 | run 8 | run 9 |
| ------------ | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- |
| steam python | 13859 | 13817 | 13227 | 14443 | 14377 | 14525 | 13436 | 13964 | 13672 | 13799 |
| steam  scala | 14840 | 15516 | 16064 | 15516 | 15416 | 16240 | 15695 | 16246 | 15082 | 15463 |
| covid python | 14344 | 13400 | 13447 | 14340 | 13724 | 13262 | 13354 | 14398 | 13666 | 13652 |
| covid  scala | 15406 | 15395 | 15660 | 16456 | 15502 | 15895 | 16574 | 16308 | 15907 | 15795 |