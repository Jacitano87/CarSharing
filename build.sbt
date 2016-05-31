name := "CarSharing"

version := "1.0"

scalaVersion := "2.11.8"

//libraryDependencies += "org.apache.spark" % "spark-core" % "1.6.1"

libraryDependencies += "io.plasmap" %% "geow" % "0.3.11-SNAPSHOT"
libraryDependencies += "org.apache.spark" %% "spark-core" % "1.6.1"
libraryDependencies += "org.apache.spark" %% "spark-graphx" % "1.6.1"
resolvers += "Sonatype OSS Snapshots" at "htt" +
  "ps://oss.sonatype.org/content/repositories/snapshots"