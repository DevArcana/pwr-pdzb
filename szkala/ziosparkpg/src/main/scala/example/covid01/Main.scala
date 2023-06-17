package covid01

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

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
