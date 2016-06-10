package com.gu.cm

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.RegionUtils
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.{PrimaryKey, DynamoDB}
import com.typesafe.config.{ConfigFactory, Config}
import scala.util.{Failure, Success, Try}

class DynamoDbConfigurationSource(dynamoDb: DynamoDB, identity: Identity, prefix: String) extends ConfigurationSource {

  val tableName = s"$prefix${identity.stack}"

  override def load: Config = {
    val config = for {
      table <- Try(dynamoDb.getTable(tableName))
      item <- Try(table.getItem(new PrimaryKey("App", identity.app, "Stage", identity.stage)))
    } yield {
      ConfigFactory.parseMap(item.getMap("Config"), s"Dynamo DB table $tableName [App=${identity.app}, Stage=${identity.stage}]")
    }

    config match {
      case Success(theConfig) => theConfig
      case Failure(theFailure) => ConfigFactory.empty(s"no DynamoDB config (or failed to load) for $tableName [App=${identity.app}, Stage=${identity.stage}], exception=[$theFailure]")
    }
  }
}

object DynamoDbConfigurationSource {
  def apply(identity: Identity, prefix: String = "config-"): DynamoDbConfigurationSource = {
    val dynamoDb = {
      val client = new AmazonDynamoDBClient(new DefaultAWSCredentialsProviderChain())
      client.setRegion(RegionUtils.getRegion(identity.region))
      new DynamoDB(client)
    }
    new DynamoDbConfigurationSource(dynamoDb, identity, prefix)
  }
}
