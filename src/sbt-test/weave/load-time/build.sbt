lazy val buildSettings = Seq(
  organization := "com.lightbend.sbt.aspectj",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.12.19"
)

lazy val sample = (project in file("."))
  .settings(buildSettings)
  .aggregate(inputs, tracer)

lazy val inputs = (project in file("inputs"))
  .settings(buildSettings)

lazy val tracer = (project in file("tracer"))
  .enablePlugins(SbtAspectj)
  .settings(buildSettings)
  .settings(
    // only compile the aspects (no weaving)
    Aspectj / aspectjCompileOnly := true,

    // add the compiled aspects as products
    Compile / products ++= (Aspectj / products).value
  )
  .dependsOn(inputs)

lazy val woven = (project in file("woven"))
  .enablePlugins(SbtAspectj)
  .settings(buildSettings)
  .settings(
    // fork the run so that javaagent option can be added
    run / fork := true,

    // add the aspectj weaver javaagent option
    run / javaOptions ++= (Aspectj / aspectjWeaverOptions).value
  ).dependsOn(inputs, tracer)

// for sbt scripted test:
TaskKey[Unit]("check") := {
  import scala.sys.process.Process

  val cp = (woven / Compile / fullClasspath).value
  val mc = (woven / Compile / mainClass).value
  val opts = (woven / Compile / run / javaOptions).value

  val LF = System.lineSeparator()
  val expected = "Printing sample:" + LF + "hello" + LF  
  val output = Process("java", opts ++ Seq("-classpath", cp.files.absString, mc getOrElse "")).!!
  if (output != expected) {
    println("Unexpected output:")
    println(output)
    println("Expected:")
    println(expected)
    sys.error("Unexpected output")
  } else {
    print(output)
  }
}
