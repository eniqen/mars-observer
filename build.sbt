name := "xing-game"

version := "0.1"

scalaVersion := "2.12.9"

lazy val ScalaTest = "3.0.8"
lazy val EnumeratumVersion = "1.5.13"
lazy val KindProjectorVersion = "0.10.3"
lazy val CatsCore = "2.0.0-RC2"
lazy val CatsEffect = "2.0.0-RC2"

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % ScalaTest,
  "com.beachape" %% "enumeratum" % EnumeratumVersion,
  "org.typelevel" %% "cats-core" % CatsCore,
  "org.typelevel" %% "cats-effect" % CatsEffect,
  "org.scalatest" %% "scalatest" % ScalaTest % "test"
)

resolvers += Resolver.sonatypeRepo("snapshots")

addCompilerPlugin(
  "org.typelevel" %% "kind-projector" % KindProjectorVersion cross CrossVersion.binary)