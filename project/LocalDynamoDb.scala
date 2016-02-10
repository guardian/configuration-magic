import com.teambytes.sbt.dynamodb.DynamoDBLocal
import com.teambytes.sbt.dynamodb.DynamoDBLocal.Keys._
import sbt._

object LocalDynamoDb {

  val settings: Seq[Setting[_]] = DynamoDBLocal.settings ++ Seq(
    dynamoDBLocalDownloadDirectory := file("dynamodb-local"),
    dynamoDBLocalInMemory := true,
    dynamoDBLocalVersion := "2015-07-16_1.0"
  )

}