package com.xing.mars.operation

import com.xing.mars.operation.actions.Command
import com.xing.mars.operation.actions.Command.{
  MoveForward,
  TurnLeft,
  TurnRight
}
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Mikhail Nemenko { @literal <nemenkoma@gmail.com>}
  */
class CommandSpec extends FlatSpec with Matchers {

  it should "split command to steps" in {
    val commands: List[Command] =
      List(TurnLeft, TurnRight, MoveForward, MoveForward, TurnLeft, TurnLeft)

    val result = Command.splitSteps(commands)

    result should contain theSameElementsInOrderAs List(
      List(TurnLeft, TurnRight, MoveForward),
      List(MoveForward),
      List(TurnLeft, TurnLeft)
    )
  }

  it should "return empty list" in {
    Command.splitSteps(Nil) shouldBe empty
  }

  it should "split each movement" in {
    val forwards = List.fill(10)(MoveForward)

    Command.splitSteps(forwards) shouldBe forwards.map(List(_))
  }

  it should "not split turn actions" in {
    val actions = List.fill(10)(TurnLeft) ::: List.fill(5)(TurnRight)

    Command.splitSteps(actions) should contain theSameElementsInOrderAs List(
      actions)
  }
}
