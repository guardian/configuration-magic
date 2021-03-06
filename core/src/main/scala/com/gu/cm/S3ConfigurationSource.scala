package com.gu.cm

import com.amazonaws.auth.{AWSCredentialsProvider, DefaultAWSCredentialsProviderChain}
import com.amazonaws.regions.RegionUtils
import com.amazonaws.regions.ServiceAbbreviations.S3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.{Failure, Success, Try}

class S3ConfigurationSource(s3: AmazonS3Client, identity: Identity, bucket: String, version: Option[Int]) extends ConfigurationSource {

  override def load: Config = {
    val configPath = versionedFilePath(identity, version)
    val request = new GetObjectRequest(bucket, configPath)
    val config = for {
      result <- Try(s3.getObject(request))
      item <- Try(scala.io.Source.fromInputStream(result.getObjectContent, "UTF-8").mkString)
    } yield {
      ConfigFactory.parseString(item)
    }

    config match {
      case Success(theConfig) => theConfig
      case Failure(theFailure) => ConfigFactory.empty(s"no s3 config (or failed to load) for bucket=$bucket path=$configPath, exception=[$theFailure]")
    }
  }

  def versionedFilePath(identity: Identity, version:Option[Int]): String = {
    val fileVersion = version.map(".v" + _).getOrElse("")
    s"config/${identity.region}-${identity.stack}$fileVersion.conf"
  }
}

object S3ConfigurationSource {
  def apply(identity: Identity, bucket: String, credentials: AWSCredentialsProvider = new DefaultAWSCredentialsProviderChain(), version: Option[Int] = None): S3ConfigurationSource = {
    val s3 = {
      val client = new AmazonS3Client(credentials)
      client.setRegion(RegionUtils.getRegion(identity.region))
      client.setEndpoint(RegionUtils.getRegion(identity.region).getServiceEndpoint(S3))
      client
    }
    new S3ConfigurationSource(s3, identity, bucket, version)
  }
}
