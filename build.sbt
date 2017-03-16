name := """akka-test"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.11"

val circeVersion = "0.7.0"

libraryDependencies ++= Seq(
  "com.pauldijou" %% "jwt-play-json" % "0.12.0",
  "org.postgresql" % "postgresql" % "42.0.0",
  "com.github.tminglei" %% "slick-pg" % "0.14.6",
  "com.github.tminglei" %% "slick-pg_date2" % "0.14.6",
  "com.typesafe.akka" %% "akka-http" % "10.0.4",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion
)

EclipseKeys.preTasks := Seq(compile in Compile)

scalacOptions ++= Seq(
  "-deprecation",           
  "-encoding", "UTF-8",       // yes, this is 2 args
  "-feature",                
  "-unchecked"
)
