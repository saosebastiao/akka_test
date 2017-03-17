name := """akka-test"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.17"

val circeVersion = "0.7.0"

//http://doc.akka.io/docs/akka/current/scala/testing.html

libraryDependencies ++= Seq(
  "com.pauldijou" %% "jwt-play-json" % "0.12.0",
  "org.postgresql" % "postgresql" % "42.0.0",
  "com.github.tminglei" %% "slick-pg" % "0.14.6",
  "com.github.tminglei" %% "slick-pg_date2" % "0.14.6",
  "com.typesafe.akka" %% "akka-http" % "10.0.5",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

EclipseKeys.preTasks := Seq(compile in Compile)

scalacOptions ++= Seq(
  "-deprecation",           
  "-encoding", "UTF-8",       // yes, this is 2 args
  "-feature",                
  "-unchecked"
)
