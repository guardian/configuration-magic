package com.gu.cm

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.RegionUtils
import com.amazonaws.regions.ServiceAbbreviations._
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.{Item, DynamoDB}
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.KeyType
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import com.amazonaws.services.dynamodbv2.model._
import com.amazonaws.services.s3.AmazonS3Client
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigFactory
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.collection.JavaConverters._

class s3ConfigurationSourceSpec extends Specification {
  "a s3 configuration Source" should {
    "look for a versioned file if a version is present" in new s3Scope {
      val version = 2
      val filePath = s3ConfigurationSource.versionedFilePath(identity, Some(version))
      filePath shouldEqual "config/eu-west-1-test-stack.v2.conf"
    }

    "look for a non versioned file if a version is not present" in new s3Scope {
      val filePath = s3ConfigurationSource.versionedFilePath(identity, None)
      filePath shouldEqual "config/eu-west-1-test-stack.conf"
    }
  }

  trait s3Scope extends Scope {

    val identity = AwsApplication(
      stack = "test-stack",
      app = "configuration-magic",
      stage = "test",
      region = "eu-west-1"
    )

    val s3 = {
      val client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain())
      client.setRegion(RegionUtils.getRegion(identity.region))
      client.setEndpoint(RegionUtils.getRegion(identity.region).getServiceEndpoint(S3))
      client
    }

    val s3ConfigurationSource = new S3ConfigurationSource(s3, identity, "frontend", Some(1))

  }
}