package com.gu.cm

import com.amazonaws.regions.{RegionUtils, Region}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class IdentitySpec extends Specification {
  "InstanceDescriber" should {
    "describe a test instance" in new InstanceDescriberScope {
      override val mode = Mode.Test
      instanceDescriber.whoAmI shouldEqual LocalApplication("Test App")
    }
    "describe a dev instance" in new InstanceDescriberScope {
      override val mode = Mode.Dev
      instanceDescriber.whoAmI shouldEqual LocalApplication("Test App")
    }
    "fail to describe a prod instance without tags" in new InstanceDescriberScope {
      instanceDescriber.whoAmI should throwA[RuntimeException]
    }
    "fail to describe a prod instance without the right tags" in new InstanceDescriberScope {
      override val instanceTags = Map("bleurg" -> "ouch")
      instanceDescriber.whoAmI should throwA[RuntimeException]
    }
    "describe a prod instance with the right tags" in new InstanceDescriberScope {
      override val instanceTags = Map("App" -> "myApp", "Stage" -> "TEST", "Stack" -> "myStack")
      instanceDescriber.whoAmI shouldEqual AwsApplication("myStack", "myApp", "TEST", "eu-west-1")
    }
  }

  trait InstanceDescriberScope extends Scope {
    def mode: Mode = Mode.Prod

    def instanceTags = Map.empty[String, String]

    def awsInstance = new AwsInstance {
      override def region: Option[Region] = Some(RegionUtils.getRegion("eu-west-1"))
      override def tags: Map[String, String] = instanceTags
    }

    def instanceDescriber = new InstanceDescriber(
      defaultAppName = "Test App",
      mode = mode,
      awsInstance = awsInstance
    )
  }
}
