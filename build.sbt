name := "Türkmen Bot"

organization := "morazow.com"

version := "1.0-SNAPSHOT"

resolvers ++= Seq("maven.org" at "http://repo2.maven.org/maven2")

libraryDependencies ++= Seq(
  "org.twitter4j" % "twitter4j-core" % "4.0.2"
)

