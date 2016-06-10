package com.gu.cm

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.{Item, DynamoDB}
import com.amazonaws.services.dynamodbv2.model._
import com.typesafe.config.ConfigFactory
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.collection.JavaConverters._

class DynamoDbConfigurationSourceSpec extends Specification {
  "a dynamo DB configuration Source" should {
    "load a config from dynamodb" in new DynamoDbScope {
      val conf = dynamoDbConfigurationSource.load

      conf.getInt("foo.a") shouldEqual 42
    }
  }

  trait DynamoDbScope extends Scope {
    val dynamoDb = {
      val client = new AmazonDynamoDBClient(new DefaultAWSCredentialsProviderChain())
      client.setEndpoint("http://localhost:8000")
      new DynamoDB(client)
    }

    val identity = AwsApplication(
      stack = "test-stack",
      app = "configuration-magic",
      stage = "test",
      region = "eu-west-1"
    )

    val dynamoDbConfigurationSource = new DynamoDbConfigurationSource(dynamoDb, identity, "prefix-")

    def initTable() = {
      val req = new CreateTableRequest(
        "prefix-test-stack",
        List(
          new KeySchemaElement("App", KeyType.HASH),
          new KeySchemaElement("Stage", KeyType.RANGE)
        ).asJava).withAttributeDefinitions(
        new AttributeDefinition("App", "S"),
        new AttributeDefinition("Stage", "S")
      ).withProvisionedThroughput(new ProvisionedThroughput(1000L, 1000L))

      val table = dynamoDb.createTable(req)

      val confBlob = ConfigFactory.parseResourcesAnySyntax("sample").root.unwrapped()

      val item = new Item()
        .withString("App", "configuration-magic")
        .withString("Stage", "test")
        .withMap("Config", confBlob)
      table.putItem(item)
    }
    initTable()
  }
}
