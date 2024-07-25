val Organization = "com.lightbend.sbt.aspectj.sample.external"
val Version = "0.1-SNAPSHOT"

lazy val buildSettings = Seq(
  organization := Organization,
  version := Version,
  scalaVersion := "2.12.19"
)

lazy val sample = (project in file("."))
  .settings(buildSettings)
  .aggregate(tracer, instrumented)

// compiled aspects (published locally for this sample)
lazy val tracer = (project in file("tracer"))
  .enablePlugins(SbtAspectj)
  .settings(buildSettings)
  .settings(
    // only compile the aspects (no weaving)
    Aspectj / aspectjCompileOnly := true,

    // ignore warnings (we don't have the target classes at this point)
    Aspectj / aspectjLintProperties += "invalidAbsoluteTypeName = ignore",

    // replace regular products with compiled aspects
    Compile / products ++= (Aspectj / products).value
  )

// use the published tracer (as if it was external)
lazy val instrumented = (project in file("instrumented"))
  .enablePlugins(SbtAspectj)
  .settings(buildSettings)
  .settings(
    // add the compiled aspects as a dependency
    libraryDependencies += Organization %% "tracer" % Version,

    // add the tracer as binary aspects for aspectj
    Aspectj / aspectjBinaries ++= update.value.matching(moduleFilter(organization = Organization, name = "tracer*")),

    // weave this project's classes
    Aspectj / aspectjInputs += (Aspectj / aspectjCompiledClasses).value,
    Compile / products := (Aspectj / products).value,
    Runtime / products := (Compile / products).value
  )

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
