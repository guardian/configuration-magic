package com.gu.cm

import play.api.{Logger => PlayLogger}

object PlayDefaultLogger extends Logger {
  override def info(message: => String): Unit = PlayLogger.info(s"[Configuration Magic] $message")
  override def warn(message: => String): Unit = PlayLogger.warn(s"[Configuration Magic] $message")
  override def error(message: => String): Unit = PlayLogger.error(s"[Configuration Magic] $message")
  override def error(message: => String, exception: => Throwable): Unit = PlayLogger.error(s"[Configuration Magic] $message", exception)
}