package com.xing.mars.operation

import cats.Show
import com.xing.mars.operation.RoverWorker.RoverId
import com.xing.mars.operation.actions.Command.{
  MoveForward,
  TurnLeft,
  TurnRight
}
import com.xing.mars.operation.actions.Direction.{East, North, South, West}
import com.xing.mars.operation.actions.{Command, Direction}
import cats.syntax.monoid._

/**
  * @author Mikhail Nemenko { @literal <nemenkoma@gmail.com>}
  */
case class RoverWorker(id: RoverId, cord: Cord, heading: Direction) {
  def info(implicit S: Show[RoverWorker]): String = S.show(this)
}

object RoverWorker {
  case class RoverId(id: Int) extends AnyVal

  implicit val showRover: Show[RoverWorker] =
    Show.show(r =>
      s"Rover #${r.id} has finished cord=${r.cord}, h=${r.heading}")

  implicit class RoverOps(val r: RoverWorker) extends AnyVal {
    def oneStepBack: RoverWorker =
      r.copy(cord = r.cord |+| r.heading.reverse.toCord)

    def move(c: Command): RoverWorker =
      c match {
        case TurnLeft    => r.copy(heading = r.heading.turnLeft)
        case TurnRight   => r.copy(heading = r.heading.turnRight)
        case MoveForward => r.copy(cord = r.cord |+| r.heading.toCord)
        case _           => r
      }

    def simulateWork(commands: Command*): RoverWorker =
      commands.foldLeft(r)(_.move(_))
  }
}
