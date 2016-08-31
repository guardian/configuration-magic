package com.gu.cm

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.RegionUtils
import com.amazonaws.regions.ServiceAbbreviations._
import com.amazonaws.services.s3.AmazonS3Client
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

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