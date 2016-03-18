package com.gu.cm

import play.api.{Logger => PlayLogger}

object PlayDefaultLogger extends Logger {
  val delegateLogger = PlayLogger("configuration-magic")
  override def info(message: => String): Unit = delegateLogger.info(message)
  override def warn(message: => String): Unit = delegateLogger.warn(message)
  override def error(message: => String): Unit = delegateLogger.error(message)
  override def error(message: => String, exception: => Throwable): Unit = delegateLogger.error(message, exception)
}