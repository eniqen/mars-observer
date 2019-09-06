package com.xing.mars.operation

/**
 * @author Mikhail Nemenko { @literal <nemenkoma@gmail.com>}
 */
sealed trait Status

object Status {
  case object Waiting extends Status
  case object Free extends Status
  case object Finish extends Status
  case object OutOfPlateau extends Status
}
