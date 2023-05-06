val scala3Version = "3.2.2"

lazy val root = project.aggregate(steam_01_combine, steam_03_takeN, steam_04_fetch, covid_01, shared)

lazy val steam_01_combine = project
  .in(file("steam_01_combine"))
  .settings(
    assembly / mainClass := Some("JoinSteamDatasets"),
    name                 := "steam_01_combine",
    commonSettings
  )
  .dependsOn(shared)

lazy val steam_02_choose = project
  .in(file("steam_02_choose"))
  .settings(
    assembly / mainClass := Some("Main"),
    name                 := "steam_02_choose",
    commonSettings
  )
  .dependsOn(shared)

lazy val steam_03_takeN = project
  .in(file("steam_03_takeN"))
  .settings(
    assembly / mainClass := Some("Main"),
    name                 := "steam_03_takeN",
    commonSettings
  )
  .dependsOn(shared)

lazy val steam_04_fetch = project
  .in(file("steam_04_fetch"))
  .settings(
    assembly / mainClass := Some("Main"),
    name                 := "steam_04_fetch",
    commonSettings
  )
  .dependsOn(shared)

lazy val covid_01 = project
  .in(file("covid_01"))
  .settings(
    assembly / mainClass := Some("Main"),
    name                 := "covid_01",
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
