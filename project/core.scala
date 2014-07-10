import sbt._
import Keys._

object CoreBuild {

  import Dependencies._

  val appName = "core"

  val settings = BuildSettings.common ++ Seq(
    libraryDependencies ++= Seq(
      scalatest,
      Akka.actor,
      Akka.testKit,
      Akka.persistance,
      Akka.contrib
    )
  )

  val `redikka-core` = project in file("redikka-core") settings (settings: _*)
}
