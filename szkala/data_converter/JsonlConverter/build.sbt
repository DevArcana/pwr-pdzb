ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"


libraryDependencies ++= Seq(
  "dev.zio"          %% "zio"           % "2.0.13",
  "dev.zio"          %% "zio-streams"   % "2.0.13",
  "dev.zio"          %% "zio-json"      % "0.5.0",
  "com.github.tototoshi"    %% "scala-csv"                         % "1.3.10",
)


lazy val root = (project in file("."))
  .settings(
    name := "JsonlConverter"
  )
