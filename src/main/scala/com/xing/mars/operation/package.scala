package com.xing.mars

import java.util.concurrent.ConcurrentHashMap

import cats.Monoid
import com.xing.mars.operation.RoverWorker.RoverId
import com.xing.mars.operation.actions.Command

/**
  * @author Mikhail Nemenko { @literal <nemenkoma@gmail.com>}
  */
package object operation {

  type ByCord[T] = ConcurrentHashMap[Cord, T]
  type ByRoverId[T] = Map[RoverId, T]
  type Step = List[Command]

  case class Cord(x: Int, y: Int)
  case class FieldStatus(id: Option[RoverId], status: Status)

  object Cord {
    implicit val cordMonoid: Monoid[Cord] = new Monoid[Cord] {
      override def empty: Cord = Cord(0, 0)
      override def combine(x: Cord, y: Cord): Cord = Cord(x.x + y.x, y.y + x.y)
    }

  }
  object FieldStatus {
    val default = FieldStatus(None, Status.Free)
  }
}
