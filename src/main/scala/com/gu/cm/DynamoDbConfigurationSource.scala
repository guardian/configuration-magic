package com.gu.cm

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.{TableKeysAndAttributes, PrimaryKey, DynamoDB}
import com.typesafe.config.{ConfigFactory, Config}
import scala.collection.JavaConverters._

class DynamoDbConfigurationSource(credProvider: AWSCredentialsProvider, region: Region, identity: Identity, prefix: String = "config-") extends ConfigurationSource {

  lazy val dynamoDb = {
    val client = new AmazonDynamoDBClient(credProvider)
    client.setRegion(region)
    new DynamoDB(client)
  }

  override def load: Config = {
    val tableName = s"$prefix${identity.stack}"
    val primaryKey = new PrimaryKey("App", identity.app, "Stage", identity.stage)

    val tableKeysAndAttributes = new TableKeysAndAttributes(tableName).withPrimaryKeys(primaryKey)

    val result = dynamoDb.batchGetItem(tableKeysAndAttributes)
    val items = result.getTableItems.asScala.get(tableName).toList.flatMap(_.asScala)
    items match {
      case Nil => ConfigFactory.empty(s"no DynamoDB config for $tableName [App=${identity.app}, Stage=${identity.stage}]")
      case item :: _ => ConfigFactory.parseMap(item.getMap("Config"), s"Dynamo DB table $tableName [App=${identity.app}, Stage=${identity.stage}]")
    }
  }
}
