package com.gu.cm

import java.io.{PrintWriter, StringWriter}

trait Logger {
  def info(message: => String): Unit
  def warn(message: => String): Unit
  def error(message: => String): Unit
  def error(message: => String, exception: => Throwable): Unit
}

object SysOutLogger extends Logger {
  def info(message: => String): Unit = println(s"[INFO] $message")
  def warn(message: => String): Unit = println(s"[WARN] $message")
  def error(message: => String): Unit = println(s"[ERROR] $message")
  def error(message: => String, exception: => Throwable): Unit = {
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    exception.printStackTrace(pw)
    println(s"[ERROR] $message caused by: ${sw.toString}")
  }
}
