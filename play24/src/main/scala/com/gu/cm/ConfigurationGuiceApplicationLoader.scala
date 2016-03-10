package com.gu.cm

import play.api.{Configuration => PlayConfiguration}
import play.api.ApplicationLoader.Context
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceApplicationLoader}
import PlayImplicits._

class ConfigurationGuiceApplicationLoader extends GuiceApplicationLoader() {

  def appName(context: Context): String = context.initialConfiguration.getString("play.application.name")
    .getOrElse(throw new RuntimeException("Please define a default application name in application.conf with the property play.application.name"))

  override protected def builder(context: Context): GuiceApplicationBuilder = {
    val config = Configuration(
      defaultAppName = appName(context),
      mode = context.environment.mode,
      logger = PlayDefaultLogger
    ).load
    PlayDefaultLogger.info(s"Configuration loaded from ${config.origin().description()}")
    super.builder(context).loadConfig(context.initialConfiguration ++ PlayConfiguration(config))
  }
}
