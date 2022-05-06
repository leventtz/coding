name := "pt-levent"

version := "0.1"

scalaVersion := "2.13.8"
val AkkaVersion = "2.6.19"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,
  "commons-codec" % "commons-codec" % "1.9"
)

libraryDependencies += "commons-codec" % "commons-codec" % "1.9"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.11"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.11" % "test"
