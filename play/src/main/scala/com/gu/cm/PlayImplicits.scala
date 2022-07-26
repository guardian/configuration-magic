package com.gu.cm

import play.api.{Mode => PlayMode}

object PlayImplicits {
  implicit def playModeToCmMode(mode: PlayMode.Mode): Mode = mode match {
    case PlayMode.Dev => Mode.Dev
    case PlayMode.Test => Mode.Test
    case PlayMode.Prod => Mode.Prod
  }
}