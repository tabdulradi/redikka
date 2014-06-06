import sbt._
import Keys._

object TestsBuild {

  import Dependencies._

  val settings = BuildSettings.common ++ Seq(
    libraryDependencies ++= Seq(
      scalatest
    ) ++ Clients.all
  )

  val `redikka-tests` = project in file("redikka-tests") settings (settings: _*)
}
