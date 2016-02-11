import com.teambytes.sbt.dynamodb.DynamoDBLocal
import sbt.Keys._

organization := "com.gu"

scalaVersion := "2.11.7"

name := "configuration-magic"

lazy val core = project
  .settings(LocalDynamoDb.settings)
  .settings(
    name := "configuration-magic-core",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.0",
      "org.specs2" %% "specs2-core" % "3.7" % "test",
      "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.10.51"
    ),
    test in Test <<= (test in Test).dependsOn(DynamoDBLocal.Keys.startDynamoDBLocal),
    testOnly in Test <<= (testOnly in Test).dependsOn(DynamoDBLocal.Keys.startDynamoDBLocal),
    testQuick in Test <<= (testQuick in Test).dependsOn(DynamoDBLocal.Keys.startDynamoDBLocal)
  )

lazy val play24 = project
  .dependsOn(core)
  .settings(
  name := "configuration-magic-play2.4",
  libraryDependencies ++= Seq(
    "com.typesafe.play" % "play_2.11" % "2.4.6"
  ))