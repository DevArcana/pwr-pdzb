ThisBuild / scalaVersion := "2.13.8"

ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val dependencies = new {
  lazy val zio      = "2.0.15"
  lazy val zioSpark = "0.12.0"
  lazy val spark    = "3.3.1"
}

lazy val root = (project in file("."))
  .settings(
    name := "ziosparkpg",
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    libraryDependencies ++= Seq(
      "dev.zio"          %% "zio"           % dependencies.zio,
      "dev.zio"          %% "zio-test"      % dependencies.zio % Test,
      "io.univalence"    %% "zio-spark"     % dependencies.zioSpark,
      "org.apache.spark" %% "spark-core"    % dependencies.spark,
      "org.apache.spark" %% "spark-sql"     % dependencies.spark,
      "org.apache.hadoop" % "hadoop-client" % dependencies.spark
    )
  )


artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  "spark.jar"
}
// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
