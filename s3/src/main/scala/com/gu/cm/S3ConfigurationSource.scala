package com.gu.cm

import java.io.InputStreamReader

import com.amazonaws.regions.RegionUtils
import com.amazonaws.services.s3.AmazonS3Client
import com.typesafe.config.{ConfigFactory, Config}

import scala.util.{Failure, Success, Try}

class S3ConfigurationSource(s3: AmazonS3Client, bucketName: String, location: String) extends ConfigurationSource {
  override def load: Config = {
    val config = for {
      s3Object <- Try(s3.getObject(bucketName, location))
      objectContent <- Try(s3Object.getObjectContent)
      reader <- Try(new InputStreamReader(objectContent))
    } yield {
      ConfigFactory.parseReader(reader)
    }
    config match {
      case Success(theConfig) => theConfig
      case Failure(theFailure) =>
        ConfigFactory.empty(s"no S3 config (or failed to load) for [Bucket=$bucketName, Location=$location], exception=[$theFailure]")
    }
  }
}

object S3ConfigurationSource {
  def apply(identity: Identity, bucketName: String): S3ConfigurationSource = {
    val s3Client = {
      val client = new AmazonS3Client()
      client.setRegion(RegionUtils.getRegion(identity.region))
      client
    }
    val location = s"${identity.stack}/${identity.stage}/${identity.app}.conf"
    new S3ConfigurationSource(s3Client, bucketName, location)
  }

  def apply(s3: AmazonS3Client, bucketName: String, location: String): S3ConfigurationSource = {
    new S3ConfigurationSource(s3, bucketName, location)
  }
}
