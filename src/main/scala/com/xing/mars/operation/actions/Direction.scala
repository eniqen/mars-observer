package com.xing.mars.operation.actions

import com.xing.mars.operation.Cord
import enumeratum._

import scala.collection.immutable._

/**
  * @author Mikhail Nemenko { @literal <nemenkoma@gmail.com>}
  */
sealed abstract class Direction(heading: String) extends EnumEntry {
  override def entryName: String = heading
}

object Direction extends Enum[Direction] {
  case object North extends Direction("N")
  case object East extends Direction("E")
  case object South extends Direction("S")
  case object West extends Direction("W")

  override def values: IndexedSeq[Direction] = findValues

  implicit class DirectionOps(val h: Direction) extends AnyVal {
    def turnRight: Direction =
      values((values.indexOf(h) + 1) % values.length)

    def turnLeft: Direction = {
      val index = values.indexOf(h) - 1
      values(if (index == -1) values.length - 1 else index % values.length)
    }

    def toCord: Cord = h match {
      case North => Cord(0, 1)
      case East  => Cord(1, 0)
      case South => Cord(0, -1)
      case West  => Cord(-1, 0)
    }

    def reverse: Direction = h match {
      case North => South
      case South => North
      case East  => West
      case West  => East
    }
  }
}
