val scala3Version = "3.2.2"

lazy val root = project
  .in(file("."))
  .settings(
    name                                      := "mapreduce",
    version                                   := "0.1.0-SNAPSHOT",
    scalaVersion                              := scala3Version,
    libraryDependencies += "org.scalameta"    %% "munit"         % "0.7.29" % Test,
    libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "3.3.5"  % "provided",
    libraryDependencies += "org.apache.hadoop" % "hadoop-core"   % "1.2.1"  % "provided",
    libraryDependencies += "dev.zio"          %% "zio"           % "2.0.13",
    libraryDependencies += "dev.zio"          %% "zio-streams"   % "2.0.13",
    libraryDependencies += "dev.zio"          %% "zio-json"      % "0.5.0"
  )

assembly / mainClass := Some("WordCount")
