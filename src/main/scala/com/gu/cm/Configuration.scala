package com.gu.cm

import com.typesafe.config._

trait ConfigurationSource {
  def load: Config
}

object EmptyConfigurationSource extends ConfigurationSource {
  def load: Config = ConfigFactory.empty()
}

class Configuration(sources: List[ConfigurationSource]) extends ConfigurationSource {
  def load: Config = {
    sources.map(_.load).foldLeft(ConfigFactory.empty()) {
      case (agg, source) => agg.withFallback(source)
    }
  }
}
