import sbt._
import Keys._

object CoreBuild {

  import Dependencies._

  val appName = "core"

  val settings = BuildSettings.common ++ Seq(
    libraryDependencies ++= Seq(
      scalatest,
      Akka.actor
    )
  )

  val `redikka-core` = project in file("redikka-core") settings (settings: _*)
}
