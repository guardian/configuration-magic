package com.gu.cm

import java.io.File

import com.gu.cm.Mode._
import com.typesafe.config._

trait ConfigurationSource {
  def load: Config
  // prepend
  def +:(source: ConfigurationSource) = {
    val thisLoad = load
    new ConfigurationSource {
      def load: Config = thisLoad.withFallback(source.load)
    }
  }
  // append
  def :+(source: ConfigurationSource) = {
    val thisLoad = load
    new ConfigurationSource {
      def load: Config = source.load.withFallback(thisLoad)
    }
  }
}

object ConfigurationSource {
  def apply(defaultAppName: String, mode: Mode = Mode.Prod): ConfigurationSource = {
    val identity = Identity.whoAmI(defaultAppName, mode)
    fromIdentity(identity, mode = mode)
  }

  def fromIdentity(identity: Identity, mode: Mode = Mode.Prod): ConfigurationSource = {
    ConfigurationSource.default(mode, identity)
  }

  def default(mode: Mode, identity: Identity): ConfigurationSource = {

    lazy val userHome = FileConfigurationSource(s"${System.getProperty("user.home")}/.configuration-magic/${identity.app}")
    lazy val devClassPath = ClassPathConfigurationSource("application-DEV")
    lazy val testClassPath = ClassPathConfigurationSource("application-TEST")
    lazy val classPath = ClassPathConfigurationSource("application")
    lazy val dynamo = DynamoDbConfigurationSource(identity)

    mode match {
      case Dev => userHome +: devClassPath +: classPath
      case Test => testClassPath +: classPath
      case Prod => dynamo +: classPath
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
