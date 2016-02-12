package com.gu.cm

import com.amazonaws.regions.{Region, Regions}
import com.typesafe.config._
import Mode._

class Configuration(
  sources: List[ConfigurationSource],
  logger: Logger = SysOutLogger) extends ConfigurationSource {
  def load: Config = {
    sources.foldLeft(ConfigFactory.empty()) { case (agg, source) =>
      val config = source.load
      if (config.isEmpty) {
        logger.warn(s"[Configuration-Magic] Nothing loaded from source: ${config.origin().description()}")
      } else {
        logger.info(s"[Configuration-Magic] loaded from: ${config.origin().description()}")
      }
      agg.withFallback(config)
    }
  }
}

object Configuration {
  def buildSources(
    mode: Mode = Mode.Prod,
    identity: Identity,
    region: Region = Region.getRegion(Regions.EU_WEST_1)): List[ConfigurationSource] = {

    lazy val userHome = FileConfigurationSource(s"${System.getProperty("user.home")}/.configuration-magic/${identity.app}")
    lazy val devClassPath = ClassPathConfigurationSource("application-DEV")
    lazy val testClassPath = ClassPathConfigurationSource("application-TEST")
    lazy val classPath = ClassPathConfigurationSource("application")
    lazy val dynamo = DynamoDbConfigurationSource(region, identity)

    mode match {
      case Dev => List(userHome, devClassPath, classPath)
      case Test => List(testClassPath, classPath)
      case Prod => List(dynamo, classPath)
    }
  }

  def apply(
    mode: Mode = Mode.Prod,
    identity: Identity,
    region: Region = Region.getRegion(Regions.EU_WEST_1),
    logger: Logger = SysOutLogger): Configuration = {
    new Configuration(buildSources(mode, identity, region), logger)
  }
}
