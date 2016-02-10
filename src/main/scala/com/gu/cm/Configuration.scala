package com.gu.cm

import com.typesafe.config._

class Configuration(sources: List[ConfigurationSource]) extends ConfigurationSource {
  def load: Config = {
    sources.map(_.load).foldLeft(ConfigFactory.empty()) {
      case (agg, source) => agg.withFallback(source)
    }
  }
}

object Configuration {
  def default(identity: Identity): List[ConfigurationSource] = List(
    FileConfigurationSource(s"${System.getProperty("user.home")}/.configuration-magic/${identity.app}")
  )
}
