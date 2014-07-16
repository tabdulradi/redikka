import sbt._
import Keys._

object TcpBuild {

  import Dependencies._

  val appName = "core"

  val settings = BuildSettings.common ++ Seq(
    libraryDependencies ++= Seq(
      scalatest,
      Akka.actor,
      Clients.scalaRedis // Serialization and Protocol
    )
  )

  val `redikka-tcp` = project in file("redikka-tcp") settings (settings: _*)
}
