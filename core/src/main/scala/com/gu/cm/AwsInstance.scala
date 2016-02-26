package com.gu.cm

import com.amazonaws.regions.{Regions, Region}
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model.{Filter, DescribeTagsRequest}
import com.amazonaws.util.EC2MetadataUtils
import scala.collection.JavaConverters._

import scala.util.{Failure, Success, Try}

trait AwsInstance {
  def tags: Map[String, String]
  def region: Option[Region]
}

class AwsInstanceImpl(logger: Logger) extends AwsInstance {
  private def safeAwsOperation[A](operation: => A, errorMessage: => String): Option[A] = Try(operation) match {
    case Success(value) => Some(value)
    case Failure(e) =>
      logger.error(errorMessage, e)
      None
  }

  lazy val region: Option[Region] = safeAwsOperation(Regions.getCurrentRegion, "Impossible to identify the regionName of the instance")
  lazy val instanceId: Option[String] = safeAwsOperation(EC2MetadataUtils.getInstanceId, "Impossible to identify the instanceId")
  lazy val ec2Client: Option[AmazonEC2Client] = region.flatMap { r =>
    safeAwsOperation(r.createClient(classOf[AmazonEC2Client], null, null), "Impossible to create the amazon ec2 client")
  }

  lazy val tags: Map[String, String] = {
    val allTags = for {
      theInstanceId <- instanceId
      theClient <- ec2Client
      tagsResult <- safeAwsOperation(theClient.describeTags(new DescribeTagsRequest().withFilters(
        new Filter("resource-type").withValues("instance"),
        new Filter("resource-id").withValues(theInstanceId)
      )), s"Impossible to describe the tags of the instance $theInstanceId")
    } yield {
      tagsResult.getTags.asScala.map{td => td.getKey -> td.getValue }.toMap
    }

    allTags.getOrElse(Map.empty)
  }
}

object AwsInstance {
  def apply(logger: Logger): AwsInstance = new AwsInstanceImpl(logger)
}
