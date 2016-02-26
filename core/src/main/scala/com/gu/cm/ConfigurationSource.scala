package com.gu.cm

import java.io.File

import com.gu.cm.Mode._
import com.typesafe.config._

trait ConfigurationSource {
  def load: Config
}

object ConfigurationSource {
  def defaultSources(
    mode: Mode,
    identity: Identity): List[ConfigurationSource] = {

    lazy val userHome = FileConfigurationSource(s"${System.getProperty("user.home")}/.configuration-magic/${identity.app}")
    lazy val devClassPath = ClassPathConfigurationSource("application-DEV")
    lazy val testClassPath = ClassPathConfigurationSource("application-TEST")
    lazy val classPath = ClassPathConfigurationSource("application")
    lazy val dynamo = DynamoDbConfigurationSource(identity)

    mode match {
      case Dev => List(userHome, devClassPath, classPath)
      case Test => List(testClassPath, classPath)
      case Prod => List(dynamo, classPath)
    }
  }
}

object EmptyConfigurationSource extends ConfigurationSource {
  val load: Config = ConfigFactory.empty()
}

class FileConfigurationSource(file: File) extends ConfigurationSource {
  override def load: Config = ConfigFactory.parseFileAnySyntax(file)
}

object FileConfigurationSource {
  def apply(file: File): FileConfigurationSource = new FileConfigurationSource(file)
  def apply(file: String): FileConfigurationSource = new FileConfigurationSource(new File(file))
}

class ClassPathConfigurationSource(path: String) extends ConfigurationSource {
  override def load: Config = ConfigFactory.parseResourcesAnySyntax(path)
}

object ClassPathConfigurationSource {
  def apply(path: String): ClassPathConfigurationSource = new ClassPathConfigurationSource(path)
}
