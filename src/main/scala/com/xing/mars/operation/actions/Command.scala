package com.xing.mars.operation.actions

import com.xing.mars.operation.Step

import scala.annotation.tailrec

/**
  * @author Mikhail Nemenko { @literal <nemenkoma@gmail.com>}
  */
sealed trait Command

object Command {
  case object TurnLeft    extends Command
  case object TurnRight   extends Command
  case object MoveForward extends Command
  case object Unknown     extends Command

  private def takeWhileInclusive(commands: List[Command])(
      pred: Command => Boolean): (Step, List[Command]) = {
    @tailrec
    def loop(commands: List[Command], fistStep: Step): (Step, List[Command]) =
      commands match {
        case x :: tail if !pred(x) && tail.nonEmpty => loop(tail, x :: fistStep)
        case x :: tail                              => (x :: fistStep, tail)
//        case Nil                                    => (fistStep, Nil)
      }

    val (first, nextSteps) = loop(commands, Nil)
    first.reverse -> nextSteps
  }

  def splitSteps(list: List[Command]): List[Step] = {
    @tailrec
    def go(list: List[Command], acc: List[Step]): List[Step] =
      list match {
        case Nil => acc
        case commands =>
          val (first, next) = takeWhileInclusive(commands)(_ == MoveForward)
          go(next, first :: acc)
      }
    go(list, Nil).reverse
  }
}
