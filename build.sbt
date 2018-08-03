
name := """StatsApp"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "com.typesafe.play" %% "anorm" % "2.5.3",
  "com.typesafe.akka" %% "akka-actor" % "2.1.1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1",
  "org.postgresql" % "postgresql" % "42.2.4"
)

