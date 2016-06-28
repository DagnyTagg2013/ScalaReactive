
// enablePlugins(JavaAppPackaging)

name := "newbie"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= Seq(
    //"sbt-idea-repo",
    "com.typesafe.akka" %% "akka-actor" % "2.4.3",
    "com.typesafe.akka" %% "akka-stream" % "2.4.3",
    "com.typesafe.akka" %% "akka-http-experimental" % "2.4.3",
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.3",
    "com.typesafe.akka" %% "akka-http-testkit" % "2.4.3",
    "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)

// Revolver.settings

fork in run := true
    