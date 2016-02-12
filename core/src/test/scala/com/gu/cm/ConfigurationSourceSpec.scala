package com.gu.cm

import java.io.File

import org.specs2.mutable.Specification

class ConfigurationSourceSpec extends Specification {

  "an empty configuration source" should {
    "be empty. Yup..." in {
      EmptyConfigurationSource.load.isEmpty should beTrue
    }
  }

  "a file configuration source" should {
    "load configuration from a file" in {
      val file = new File("core/src/test/resources/sample")
      val conf = FileConfigurationSource(file).load
      conf.isEmpty should beFalse
      conf.getInt("foo.a") shouldEqual 42
    }
  }

  "a classpath configuration source" should {
    "load configuration from the classpath" in {
      val cp = ClassPathConfigurationSource("sample").load
      cp.isEmpty should beFalse
      cp.getInt("foo.a") shouldEqual 42
    }
  }

}
