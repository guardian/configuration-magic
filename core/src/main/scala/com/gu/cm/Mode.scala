package com.gu.cm

sealed trait Mode

object Mode {
  case object Prod extends Mode
  case object Test extends Mode
  case object Dev extends Mode
}
