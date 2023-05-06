val scala3Version = "3.2.2"

lazy val root =
  project.aggregate(
    steam_01_combine,
    steam_02_choose,
    steam_03_takeN,
    steam_04_fetch,
    steam_05_merge_time,
    covid_01,
    covid_02,
    covid_03,
    final_join,
    shared
  )

lazy val steam_01_combine = project
  .in(file("steam_01_combine"))
  .settings(
    name                          := "steam_01_combine",
    assembly / assemblyJarName    := s"${name.value}.jar",
    assembly / mainClass          := Some("JoinSteamDatasets"),
    assembly / assemblyOutputPath := file(s"jars/${(assembly / assemblyJarName).value}"),
    commonSettings
  )
  .dependsOn(shared)

lazy val steam_02_choose = project
  .in(file("steam_02_choose"))
  .settings(
    name                          := "steam_02_choose",
    assembly / assemblyJarName    := s"${name.value}.jar",
    assembly / mainClass          := Some("Main"),
    assembly / assemblyOutputPath := file(s"jars/${(assembly / assemblyJarName).value}"),
    commonSettings
  )
  .dependsOn(shared)

lazy val steam_03_takeN = project
  .in(file("steam_03_takeN"))
  .settings(
    name                          := "steam_03_takeN",
    assembly / assemblyJarName    := s"${name.value}.jar",
    assembly / mainClass          := Some("Main"),
    assembly / assemblyOutputPath := file(s"jars/${(assembly / assemblyJarName).value}"),
    commonSettings
  )
  .dependsOn(shared)

lazy val steam_04_fetch = project
  .in(file("steam_04_fetch"))
  .settings(
    name                          := "steam_04_fetch",
    assembly / assemblyJarName    := s"${name.value}.jar",
    assembly / mainClass          := Some("Main"),
    assembly / assemblyOutputPath := file(s"jars/${(assembly / assemblyJarName).value}"),
    commonSettings
  )
  .dependsOn(shared)

lazy val steam_05_merge_time = project
  .in(file("steam_05_merge_time"))
  .settings(
    name                          := "steam_05_merge_time",
    assembly / assemblyJarName    := s"${name.value}.jar",
    assembly / mainClass          := Some("Main"),
    assembly / assemblyOutputPath := file(s"jars/${(assembly / assemblyJarName).value}"),
    commonSettings
  )
  .dependsOn(shared)

lazy val covid_01 = project
  .in(file("covid_01"))
  .settings(
    name                          := "covid_01",
    assembly / assemblyJarName    := s"${name.value}.jar",
    assembly / mainClass          := Some("Main"),
    assembly / assemblyOutputPath := file(s"jars/${(assembly / assemblyJarName).value}"),
    commonSettings
  )
  .dependsOn(shared)

lazy val covid_02 = project
  .in(file("covid_02"))
  .settings(
    name                          := "covid_02",
    assembly / assemblyJarName    := s"${name.value}.jar",
    assembly / mainClass          := Some("Main"),
    assembly / assemblyOutputPath := file(s"jars/${(assembly / assemblyJarName).value}"),
    commonSettings
  )
  .dependsOn(shared)

lazy val covid_03 = project
  .in(file("covid_03"))
  .settings(
    name                          := "covid_03",
    assembly / assemblyJarName    := s"${name.value}.jar",
    assembly / mainClass          := Some("Main"),
    assembly / assemblyOutputPath := file(s"jars/${(assembly / assemblyJarName).value}"),
    commonSettings
  )
  .dependsOn(shared)

lazy val youtube_01 = project
  .in(file("youtube_01"))
  .settings(
    name                          := "youtube_01",
    assembly / assemblyJarName    := s"${name.value}.jar",
    assembly / mainClass          := Some("Main"),
    assembly / assemblyOutputPath := file(s"jars/${(assembly / assemblyJarName).value}"),
    commonSettings
  )
  .dependsOn(shared)

lazy val final_join = project
  .in(file("final_join"))
  .settings(
    name                          := "final_join",
    assembly / assemblyJarName    := s"${name.value}.jar",
    assembly / mainClass          := Some("Main"),
    assembly / assemblyOutputPath := file(s"jars/${(assembly / assemblyJarName).value}"),
    commonSettings
  )
  .dependsOn(shared)

lazy val shared = project
  .in(file("shared"))
  .settings(
    name := "shared",
    commonSettings
  )

lazy val commonSettings = Seq(
  version                                   := "1",
  scalaVersion                              := scala3Version,
  libraryDependencies += "org.scalameta"    %% "munit"         % "0.7.29" % Test,
  libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "3.3.5"  % "provided",
  libraryDependencies += "org.apache.hadoop" % "hadoop-core"   % "1.2.1"  % "provided",
  libraryDependencies += "dev.zio"          %% "zio"           % "2.0.13",
  libraryDependencies += "dev.zio"          %% "zio-streams"   % "2.0.13",
  libraryDependencies += "dev.zio"          %% "zio-json"      % "0.5.0",
  libraryDependencies += "dev.zio"          %% "zio-http"      % "3.0.0-RC1",
  libraryDependencies += "org.scalatest"    %% "scalatest"     % "3.2.11" % Test,
  assemblyMergeStrategy in assembly         := {
    case x if x.contains("io.netty") => MergeStrategy.discard
    case x                           =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
)
