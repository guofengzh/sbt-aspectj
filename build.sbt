lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    Seq(
      organization := "com.lightbend.sbt",
      name := "sbt-aspectj",
      scalacOptions ++= Seq("-unchecked", "-deprecation", "-target:jvm-1.8"),
      scalaVersion := "2.12.19",
      crossSbtVersions := Vector("1.10.1", "1.9.1"),
      libraryDependencies ++= Seq(
        "org.aspectj" % "aspectjtools" % "1.9.22.1"
      ),
      publishMavenStyle := false,
      sbtPlugin := true,
      scriptedBufferLog := false,
      scriptedDependencies := publishLocal.value,
      scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value, s"-Dproject.version=${version.value}")
    )
  )

import ReleaseTransformations._
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  releaseStepCommandAndRemaining("^ scripted"),
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("^ publish"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
