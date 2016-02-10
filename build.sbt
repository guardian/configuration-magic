import com.teambytes.sbt.dynamodb.DynamoDBLocal

name := "configuration-magic"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.0",
  "org.specs2" %% "specs2-core" % "3.7" % "test",
  "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.10.51"
)

LocalDynamoDb.settings

test in Test <<= (test in Test).dependsOn(DynamoDBLocal.Keys.startDynamoDBLocal)

testOnly in Test <<= (testOnly in Test).dependsOn(DynamoDBLocal.Keys.startDynamoDBLocal)

testQuick in Test <<= (testQuick in Test).dependsOn(DynamoDBLocal.Keys.startDynamoDBLocal)