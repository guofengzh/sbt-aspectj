import com.lightbend.sbt.SbtAspectj.aspectjUseInstrumentedClasses

organization := "com.lightbend.sbt.aspectj"
version := "0.1-SNAPSHOT"
scalaVersion := "2.12.19"

enablePlugins(SbtAspectj)

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.16"

// add akka-actor as an aspectj input (find it in the update report)
Aspectj / aspectjInputs ++= update.value.matching(moduleFilter(organization = "com.typesafe.akka", name = "akka-actor*"))

// replace the original akka-actor jar with the instrumented classes in runtime
Runtime / fullClasspath := aspectjUseInstrumentedClasses(Runtime).value

// for sbt scripted test:
TaskKey[Unit]("check") := {
  import scala.sys.process.Process

  val cp = (Runtime / fullClasspath).value
  val mc = (Runtime / mainClass).value
  val opts = (Compile / run / javaOptions).value

  val LF = System.lineSeparator()
  val expected = "Actor asked world" + LF + "hello world" + LF
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
