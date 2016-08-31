
// enablePlugins(JavaAppPackaging)

name := "newbie"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-stream" % "2.4.9",
    "com.typesafe.akka" %% "akka-http-core" % "2.4.9",
    "com.hunorkovacs" %% "koauth" % "1.1.0",
    "org.json4s" %% "json4s-native" % "3.4.0",
    "com.typesafe.akka" %% "akka-http-testkit" % "2.4.9",
    "com.typesafe.akka" %% "akka-stream-testkit" % "2.4.9",
    // TODO:  find out if below is still needed for routing DSL
    "com.typesafe.akka" %% "akka-http-experimental" % "2.4.9",
    "org.scalatest" %% "scalatest" % "3.0.0-SNAP13" % "test",
    "com.typesafe.play" %% "play-json" % "2.5.5"
)

// TODO:  find out what this fork does exactly; ie run in different thread than build?
// Revolver.settings
fork in run := true
    