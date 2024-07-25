lazy val buildSettings = Seq(
  organization := "com.lightbend.sbt.aspectj",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.12.19"
)

lazy val sample = (project in file("."))
  .settings(buildSettings)
  .aggregate(tracer, instrumented)

// precompiled aspects
lazy val tracer = (project in file("tracer"))
  .enablePlugins(SbtAspectj)
  .settings(buildSettings)
  .settings(
    // stop after compiling the aspects (no weaving)
    Aspectj / aspectjCompileOnly := true,

    // ignore warnings (we don't have the sample classes)
    Aspectj / aspectjLintProperties += "invalidAbsoluteTypeName = ignore",

    // replace regular products with compiled aspects
    Compile / products := (Aspectj / products).value
  )

// test that the instrumentation works
lazy val instrumented = (project in file("instrumented"))
  .enablePlugins(SbtAspectj)
  .settings(buildSettings)
  .settings(
    // add the compiled aspects from tracer
    Aspectj / aspectjBinaries ++= (tracer / Compile / products).value,

    // weave this project's classes
    Aspectj / aspectjInputs += (Aspectj / aspectjCompiledClasses).value,
    Compile / products := (Aspectj / products).value,
    Runtime / products := (Compile / products).value
  ).dependsOn(tracer)

// for sbt scripted test:
TaskKey[Unit]("check") := {
  import scala.sys.process.Process

  val cp = (instrumented / Compile / fullClasspath).value
  val mc = (instrumented / Compile / mainClass).value
  val opts = (instrumented / Compile / run / javaOptions).value

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
