package com.gu.cm

import play.api.{Logger => PlayLogger}

object PlayDefaultLogger extends Logger {
  override def info(message: => String): Unit = PlayLogger.info(message)
  override def warn(message: => String): Unit = PlayLogger.warn(message)
  override def error(message: => String): Unit = PlayLogger.error(message)
  override def error(message: => String, exception: => Throwable): Unit = PlayLogger.error(message, exception)
}