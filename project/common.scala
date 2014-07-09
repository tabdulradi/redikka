import sbt._
import Keys._


object BuildSettings {
  val common = Seq(
    organization := s"com.abdulradi",
    homepage := Some(url("https://github.com/tabdulradi/")),
    resolvers ++= Resolvers.all
  )
}

object Resolvers {
  val typesafe = "typesafe.com" at "http://repo.typesafe.com/typesafe/releases/"

  val typesafeSnapshots = "typesafeSnapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

  val sonatype = "sonatype" at "http://oss.sonatype.org/content/repositories/releases"

  val chrisdinn = "chrisdinn.github" at "http://chrisdinn.github.io/releases/" // For brando test Client

  val all = Seq(typesafe, typesafeSnapshots, sonatype, chrisdinn)
}


object Dependencies {

  val scalatest = "org.scalatest" %% "scalatest" % "2.1.5" % "test"

  object Clients {
    val scalaRedis = "net.debasishg" %% "redisclient" % "2.12"
    val brando = "com.digital-achiever" %% "brando" % "1.0.2"

    val all = Seq(scalaRedis, brando)
  }

  object Akka {
    val version = "2.4-20140624-230855"
    val agent = "com.typesafe.akka" %% "akka-agent" % version
    val testKit = "com.typesafe.akka" %% "akka-testkit" % version % "test"
    val actor = "com.typesafe.akka" %% "akka-actor" % version
    val cluster = "com.typesafe.akka" %% "akka-cluster" % version
    val contrib = "com.typesafe.akka" %% "akka-contrib" % version
    val persistance = "com.typesafe.akka" %% "akka-persistence-experimental" % version
  }

}
