import ReleaseTransformations._

lazy val archerySettings = Seq(
  organization := "org.spire-math",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  homepage := Some(url("http://github.com/non/archery")),
  scalaVersion := "2.13.0",
  crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.8", "2.13.0"),
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros",
    "-unchecked",
    "-Ywarn-dead-code"
  ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, x)) if x < 13 => Seq(
      "-Xfatal-warnings",
      "-Xlint",
      "-Xfuture"
    )
    case _ => Seq()
  }),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.8" % "test",
    "org.scalacheck" %% "scalacheck" % "1.14.0" % "test"),
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := Function.const(false),
  publishTo := Some(if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging),
  pomExtra := (
    <scm>
      <url>git@github.com:non/chain.git</url>
      <connection>scm:git:git@github.com:non/chain.git</connection>
    </scm>
    <developers>
      <developer>
        <id>non</id>
        <name>Erik Osheim</name>
        <url>http://github.com/non/</url>
      </developer>
    </developers>
  ),

  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    pushChanges))

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false)

lazy val root = project
  .in(file("."))
  .aggregate(core, benchmark)
  .settings(name := "archery-root")
  .settings(archerySettings: _*)
  .settings(noPublishSettings: _*)

lazy val core = project
  .in(file("core"))
  .settings(name := "archery")
  .settings(archerySettings: _*)

lazy val benchmark = project
  .in(file("benchmark"))
  .dependsOn(core)
  .settings(name := "archery-benchmark")
  .settings(archerySettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    fork in run := true,
    javaOptions in run += "-Xmx4G",
    libraryDependencies ++= Seq(
      "ichi.bench" % "thyme" % "0.1.1" from "http://plastic-idolatry.com/jars/thyme-0.1.1.jar"))
