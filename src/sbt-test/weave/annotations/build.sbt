
organization := "com.lightbend.sbt.aspectj"
version := "0.1-SNAPSHOT"
scalaVersion := "2.12.19"

enablePlugins(SbtAspectj)

// add compiled classes as an input to aspectj
Aspectj / aspectjInputs += (Aspectj / aspectjCompiledClasses).value

// use the results of aspectj weaving
Compile / products := (Aspectj / products).value
Runtime / products := (Compile / products).value

// for sbt scripted test:
TaskKey[Unit]("check") := {
  import scala.sys.process.Process

  val cp = (Compile / fullClasspath).value
  val mc = (Compile / mainClass).value
  val opts = (Compile / run / javaOptions).value

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
