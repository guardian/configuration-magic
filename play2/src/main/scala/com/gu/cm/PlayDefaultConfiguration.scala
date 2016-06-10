package com.gu.cm

import play.api.{Configuration => PlayConfiguration}
import play.api.ApplicationLoader.Context
import PlayImplicits._

class PlayDefaultConfiguration(context: Context) extends Configuration {
  override lazy val mode: Mode = context.environment.mode
  override lazy val defaultAppName: String = context.initialConfiguration.getString("play.application.name")
    .getOrElse(throw new RuntimeException("Please define a default application name in application.conf with the property play.application.name"))
  override lazy val logger: Logger = PlayDefaultLogger
  override lazy val identity: Identity = Identity.whoAmI(defaultAppName, mode, logger)
  override lazy val sources: ConfigurationSources = new DefaultConfigurationSources(identity)

  def loadContext = context.copy(initialConfiguration = context.initialConfiguration ++ PlayConfiguration(load))
}
