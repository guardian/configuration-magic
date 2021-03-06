import com.teambytes.sbt.dynamodb.DynamoDBLocal
import sbt.Keys._
import ReleaseTransformations._

organization := "com.gu"

name := "configuration-magic"

lazy val sharedSettings = Seq(
  scalaVersion := "2.12.3",
  crossScalaVersions := Seq("2.11.11", "2.12.3"),
  organization := "com.gu",
  pomExtra in Global := {
    <url>https://github.com/guardian/configuration-magic</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>
    <scm>
      <connection>scm:git:github.com/guardian/configuration-magic</connection>
      <developerConnection>scm:git:git@github.com:guardian/configuration-magic</developerConnection>
      <url>github.com/guardian/configuration-magic</url>
    </scm>
    <developers>
      <developer>
        <id>TheGuardian</id>
        <name>TheGuardian</name>
        <url>http://www.theguardian.com</url>
      </developer>
    </developers>
  }
)

val AwsSdkVersion = "1.11.35"

lazy val core = project
  .settings(LocalDynamoDb.settings)
  .settings(sharedSettings:_*)
  .settings(
    name := "configuration-magic-core",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.0",
      "org.specs2" %% "specs2-core" % "3.9.5" % "test",
      "com.amazonaws" % "aws-java-sdk-dynamodb" % AwsSdkVersion,
      "com.amazonaws" % "aws-java-sdk-ec2" % AwsSdkVersion
    ),
    test in Test <<= (test in Test).dependsOn(DynamoDBLocal.Keys.startDynamoDBLocal),
    testOnly in Test <<= (testOnly in Test).dependsOn(DynamoDBLocal.Keys.startDynamoDBLocal),
    testQuick in Test <<= (testQuick in Test).dependsOn(DynamoDBLocal.Keys.startDynamoDBLocal)
  )

lazy val play26 = project
  .dependsOn(core)
  .settings(sharedSettings:_*)
  .settings(
    name := "configuration-magic-play2.6",
    libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play" % "2.6.6",
    "com.typesafe.play" %% "play-guice" % "2.6.6"
  ))

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("publishSigned", _)),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
  pushChanges
)
