package com.gu.cm

import com.gu.cm.Mode.{Dev, Test, Prod}

sealed trait Identity {
  def stack: String
  def app: String
  def stage: String
  def region: String
}

case class LocalApplication(app: String) extends Identity {
  val stack = "local"
  val stage = "DEV"
  val region = "None"
  override def toString = "Development or testing machine"
}

case class AwsApplication(
  stack: String,
  app: String,
  stage: String,
  region: String
) extends Identity {
  override def toString = s"AwsApplication: stack = $stack, app = $app, stage = $stage, region = $region"
}

class InstanceDescriber(
  defaultAppName: String,
  mode: Mode,
  awsInstance: => AwsInstance) extends Logging {

  def whoAmI: Identity = {
    val identity = mode match {
      case Prod => describeAwsInstance
      case Test | Dev => LocalApplication(defaultAppName)
    }
    log.info(s"Application identified as $identity")
    identity
  }

  def describeAwsInstance: Identity = {
    val identity = for {
      app <- awsInstance.tags.get("App")
      stack <- awsInstance.tags.get("Stack")
      stage <- awsInstance.tags.get("Stage")
      region <- awsInstance.region
    } yield AwsApplication(
      app = app,
      stack = stack,
      stage = stage,
      region = region.getName
    )
    identity.getOrElse(throw new RuntimeException("Unable to identify aws instance in production mode. Check if the tags are set correctly"))
  }
}

object Identity {
  def whoAmI(defaultAppName: String, mode: Mode): Identity =
    new InstanceDescriber(defaultAppName, mode, AwsInstance.apply).whoAmI
}