import NativePackagerKeys._

packageArchetype.java_application

name := "turkmen-bot"

organization := "morazow.com"

version := "1.0.0"

scalaVersion := "2.10.3"

resolvers ++= Seq("maven.org" at "http://repo2.maven.org/maven2")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.4",
  "org.twitter4j" % "twitter4j-core" % "4.0.2"
)

