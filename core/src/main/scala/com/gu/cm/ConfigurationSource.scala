package com.gu.cm

import java.io.File

import com.gu.cm.Mode._
import com.typesafe.config._

trait ConfigurationSource {
  def load: Config
}

trait ConfigurationSources {
  def resolve: PartialFunction[Mode, List[ConfigurationSource]]
}

class DefaultConfigurationSources(identity: Identity) extends ConfigurationSources {
  lazy val userHome = FileConfigurationSource(s"${System.getProperty("user.home")}/.configuration-magic/${identity.app}")
  lazy val devClassPath = ClassPathConfigurationSource("application-DEV")
  lazy val testClassPath = ClassPathConfigurationSource("application-TEST")
  lazy val classPath = ClassPathConfigurationSource("application")

  def resolve: PartialFunction[Mode, List[ConfigurationSource]] = {
    case Dev => List(userHome, devClassPath, classPath)
    case Test => List(testClassPath, classPath)
    case Prod => List(classPath)
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
