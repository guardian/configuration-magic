package com.gu.cm

import com.typesafe.config._

class Configuration(
  sources: List[ConfigurationSource],
  logger: Logger = SysOutLogger) extends ConfigurationSource {
  def load: Config = {
    sources.foldLeft(ConfigFactory.empty()) { case (agg, source) =>
      val config = source.load
      if (config.isEmpty) {
        logger.warn(s"Nothing loaded from source: ${config.origin().description()}")
      } else {
        logger.info(s"Loaded from: ${config.origin().description()}")
      }
      agg.withFallback(config)
    }
  }
}

object Configuration {
  def apply(defaultAppName: String, mode: Mode = Mode.Prod, logger: Logger = SysOutLogger): Configuration = {
    val identity = Identity.whoAmI(defaultAppName, mode, logger)
    fromIdentity(identity, mode = mode, logger = logger)
  }

  def fromIdentity(identity: Identity, mode: Mode = Mode.Prod, logger: Logger = SysOutLogger): Configuration = {
    val configurationSources = ConfigurationSource.defaultSources(mode, identity)
    new Configuration(configurationSources, logger)
  }
}
