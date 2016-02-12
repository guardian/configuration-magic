package com.gu.cm

import play.api.{Configuration => PlayConfigruation}
import play.api.ApplicationLoader.Context
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceApplicationLoader}
import PlayImplicits._

class ConfigurationGuiceApplicationLoader extends GuiceApplicationLoader() {

  def identity(context: Context): Identity = Identity(
    stack = context.initialConfiguration.getString("application.stack").getOrElse("defaultStack"),
    app = context.initialConfiguration.getString("application.app").getOrElse("defaultApplication"),
    stage = context.initialConfiguration.getString("application.stage").getOrElse("defaultStage")
  )

  override protected def builder(context: Context): GuiceApplicationBuilder = {
    val config = Configuration(
      mode = context.environment.mode,
      identity = identity(context),
      logger = PlayDefaultLogger
    ).load
    PlayDefaultLogger.info(s"[Configuration-Magic] Configuration loaded from ${config.origin().description()}")
    super.builder(context).loadConfig(context.initialConfiguration ++ PlayConfigruation(config))
  }
}