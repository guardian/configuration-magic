package com.gu.cm

import play.api.{Configuration => PlayConfiguration}
import play.api.ApplicationLoader.Context
import PlayImplicits._

object ContextConfigurationLoader {
  def apply(defaultAppName: String, context: Context): Context = {
    val config = Configuration(
      defaultAppName = defaultAppName,
      mode = context.environment.mode,
      logger = PlayDefaultLogger
    ).load.resolve()
    PlayDefaultLogger.info(s"Configuration loaded from ${config.origin().description()}")
    context.copy(initialConfiguration = context.initialConfiguration ++ PlayConfiguration(config))
  }
}
