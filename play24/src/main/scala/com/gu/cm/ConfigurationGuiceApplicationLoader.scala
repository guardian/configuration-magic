package com.gu.cm

import play.api.ApplicationLoader.Context
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceApplicationLoader}

class ConfigurationGuiceApplicationLoader extends GuiceApplicationLoader() {

  def appName(context: Context): String = context.initialConfiguration.getString("play.application.name")
    .getOrElse(throw new RuntimeException("Please define a default application name in application.conf with the property play.application.name"))

  override protected def builder(context: Context): GuiceApplicationBuilder = {
    val contextWithConfiguration = ConfigurationLoader.playContext(appName(context), context)
    super.builder(contextWithConfiguration)
  }
}
