package com.gu.cm

import com.typesafe.config._

trait Configuration extends ConfigurationSource {
  def mode: Mode
  def defaultAppName: String
  def logger: Logger
  def identity: Identity
  def sources: ConfigurationSources

  def load: Config = {
    val resolvedSources = sources.resolve(mode)
    resolvedSources.foldLeft(ConfigFactory.empty()) { case (agg, source) =>
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

class DefaultConfiguration(
  override val defaultAppName: String,
  override val mode: Mode
) extends Configuration {
  override lazy val logger: Logger = SysOutLogger
  override lazy val identity: Identity = Identity.whoAmI(defaultAppName, mode, logger)
  def sources: ConfigurationSources = new DefaultConfigurationSources(identity)
}

object Configuration {
  def apply(defaultAppName: String, mode: Mode): Configuration = {
    new DefaultConfiguration(defaultAppName, mode)
  }
}
