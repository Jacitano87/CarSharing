name := "CarSharing"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "1.6.1"

libraryDependencies += "io.plasmap" %% "geow" % "0.3.11-SNAPSHOT"

resolvers += "Sonatype OSS Snapshots" at "htt" +
  "ps://oss.sonatype.org/content/repositories/snapshots"