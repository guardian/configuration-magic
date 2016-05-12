package com.gu.cm

import play.api.{Configuration => PlayConfiguration}
import play.api.ApplicationLoader.Context
import play.api.Mode.{Mode => PlayMode}
import PlayImplicits._

object ConfigurationLoader extends Logging {
  def playContext(defaultAppName: String, context: Context): Context = {
    val config = playConfig(defaultAppName, context.environment.mode)
    context.copy(initialConfiguration = context.initialConfiguration ++ config)
  }

  def playContext(identity: Identity, context: Context): Context = {
    val config = playConfig(identity, context.environment.mode)
    context.copy(initialConfiguration = context.initialConfiguration ++ config)
  }

  def playConfig(defaultAppName: String, mode: PlayMode): PlayConfiguration = {
    val config = ConfigurationSource(
      defaultAppName = defaultAppName,
      mode = mode
    ).load.resolve()
    log.info(s"Configuration loaded from ${config.origin().description()}")
    PlayConfiguration(config)
  }

  def playConfig(identity: Identity, mode: PlayMode): PlayConfiguration = {
    val config = ConfigurationSource.fromIdentity(
      identity = identity,
      mode = mode
    ).load.resolve()
    log.info(s"Configuration loaded from ${config.origin().description()}")
    PlayConfiguration(config)
  }

}
