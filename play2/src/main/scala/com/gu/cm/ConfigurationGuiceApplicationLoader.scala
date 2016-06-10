package com.gu.cm

import play.api.ApplicationLoader.Context
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceApplicationLoader}

class ConfigurationGuiceApplicationLoader extends GuiceApplicationLoader() {
  override protected def builder(context: Context): GuiceApplicationBuilder = {
    val config = new PlayDefaultConfiguration(context)
    super.builder(config.loadContext)
  }
}
