organization in ThisBuild := "io.estatico"

lazy val coercible = project.in(file("."))
  .aggregate(core)

lazy val core = module("core")

lazy val defaultScalacOptions = Seq(
  "-Xfatal-warnings",
  "-unchecked",
  "-feature",
  "-deprecation",
  "-language:higherKinds",
  "-language:implicitConversions"
)

lazy val defaultTestDependencies = Seq(
  "org.scalacheck" %% "scalacheck" % "1.13.4",
  "org.scalatest" %% "scalatest" % "3.0.0",
  "org.scalaz" %% "scalaz-core" % "7.2.15"
).map(_ % "test")

def module(path: String) = {
  // Convert path from lisp-case to camelCase
  val id = path.split("-").reduce(_ + _.capitalize)
  // Convert path from list-case to "space case"
  val docName = path.replace('-', ' ')
  // Set default and module-specific settings.
  applyDefaultSettings(Project(id, file(path))).settings(
    name := "Coercible " + docName,
    moduleName := "coercible-" + path,
    description := "coercible" + docName
  )
}

def applyDefaultSettings(project: Project) = project.settings(
  scalacOptions ++= defaultScalacOptions,
  libraryDependencies ++= defaultTestDependencies,
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")
)
