package com.gu.cm


import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClient
import com.amazonaws.services.simplesystemsmanagement.model.{GetParametersByPathRequest, GetParametersByPathResult, Parameter}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.collection.JavaConverters._
import scala.collection.immutable.ListMap


class SystemsManagerParametersConfigurationSourceSpec extends Specification {
  "a Systems Manager Parameters configuration Source" should {
    "load a config from Systems Manager Parameters" in new SystemsManagerParametersScope(Map(
      "/test-stack/prod/configuration-magic/bar" -> "evenbiggersecret",
      "/test-stack/prod/configuration-magic/foo.a" -> "142",
      "/test-stack/test/configuration-magic/bar" -> "secret",
      "/test-stack/test/configuration-magic/foo.a" -> "42"
    )) {
      val conf = systemsManagerParametersConfigurationSource.load

      conf.entrySet().size shouldEqual 2

      conf.getString("bar") shouldEqual "secret"
      conf.getInt("foo.a") shouldEqual 42
    }

    "load a config from Systems Manager Parameters with more than 10 parameters" in new SystemsManagerParametersScope(Map(
      "/test-stack/prod/configuration-magic/foo.1" -> "60",
      "/test-stack/prod/configuration-magic/foo.2" -> "61",
      "/test-stack/test/configuration-magic/foo.1" -> "42",
      "/test-stack/test/configuration-magic/foo.2" -> "43",
      "/test-stack/test/configuration-magic/foo.3" -> "44",
      "/test-stack/test/configuration-magic/foo.4" -> "45",
      "/test-stack/test/configuration-magic/foo.5" -> "46",
      "/test-stack/test/configuration-magic/foo.6" -> "47",
      "/test-stack/test/configuration-magic/foo.7" -> "48",
      "/test-stack/test/configuration-magic/foo.8" -> "49",
      "/test-stack/test/configuration-magic/foo.9" -> "50",
      "/test-stack/test/configuration-magic/foo.10" -> "51",
      "/test-stack/test/configuration-magic/foo.11" -> "52",
      "/test-stack/test/configuration-magic/foo.12" -> "53",
      "/test-stack/test/configuration-magic/foo.13" -> "54",
      "/test-stack/test/configuration-magic/foo.14" -> "55",
      "/test-stack/test/configuration-magic/foo.15" -> "56"
    )) {
      val conf = systemsManagerParametersConfigurationSource.load

      conf.entrySet().size shouldEqual 15

      conf.getInt("foo.1") shouldEqual 42
      conf.getInt("foo.2") shouldEqual 43
      conf.getInt("foo.3") shouldEqual 44
      conf.getInt("foo.4") shouldEqual 45
      conf.getInt("foo.5") shouldEqual 46
      conf.getInt("foo.6") shouldEqual 47
      conf.getInt("foo.7") shouldEqual 48
      conf.getInt("foo.8") shouldEqual 49
      conf.getInt("foo.9") shouldEqual 50
      conf.getInt("foo.10") shouldEqual 51
      conf.getInt("foo.11") shouldEqual 52
      conf.getInt("foo.12") shouldEqual 53
      conf.getInt("foo.13") shouldEqual 54
      conf.getInt("foo.14") shouldEqual 55
      conf.getInt("foo.15") shouldEqual 56
    }
  }

  class SystemsManagerParametersScope(val values: Map[String, String]) extends Scope {

    val identity = AwsApplication(
      stack = "test-stack",
      app = "configuration-magic",
      stage = "test",
      region = "eu-west-1"
    )

    class SimpleSystemsManagementParameterStore(values: Map[String, String]) extends AWSSimpleSystemsManagementClient {

      override def getParametersByPath(request: GetParametersByPathRequest): GetParametersByPathResult = {
        val offset = Option(request.getNextToken).map(Integer.valueOf).getOrElse(Integer.valueOf(0))

        val sortedParameters = ListMap(values.filter(v => v._1.startsWith(request.getPath)).toSeq.sortBy(_._1):_*)
        val parameters = sortedParameters.slice(offset, offset + request.getMaxResults).map(v => new Parameter().withName(v._1).withValue(v._2))

        val response = new GetParametersByPathResult().withParameters(parameters.asJavaCollection)

        if (sortedParameters.size > offset + request.getMaxResults) {
          response.withNextToken((request.getMaxResults + offset).toString)
        } else {
          response.withNextToken(null)
        }

        response
      }
    }

    val systemsManager = new SimpleSystemsManagementParameterStore(values)

    val systemsManagerParametersConfigurationSource = new SystemsManagerParametersConfigurationSource(systemsManager, identity)
  }
}
