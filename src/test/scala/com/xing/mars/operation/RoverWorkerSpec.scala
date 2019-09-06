package com.xing.mars.operation

import com.xing.mars.operation.RoverWorker.RoverId
import com.xing.mars.operation.actions.Command.{MoveForward, TurnRight, Unknown}
import com.xing.mars.operation.actions.Direction.{East, North, South, West}
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Mikhail Nemenko { @literal <nemenkoma@gmail.com>}
  */
class RoverWorkerSpec extends FlatSpec with Matchers {

  val cordDefault = Cord(0, 0)

  it should "correct get back when direction is North" in {
    val rover = RoverWorker(RoverId(1), cordDefault, North)
    rover.oneStepBack shouldBe RoverWorker(rover.id, Cord(0, -1), North)
  }

  it should "correct get back when direction is East" in {
    val rover = RoverWorker(RoverId(1), cordDefault, East)
    rover.oneStepBack shouldBe RoverWorker(RoverId(1), Cord(-1, 0), East)
  }

  it should "correct get back when direction is South" in {
    val rover = RoverWorker(RoverId(1), cordDefault, South)
    rover.oneStepBack shouldBe RoverWorker(RoverId(1), Cord(0, 1), South)
  }

  it should "correct get back when direction is West" in {
    val rover = RoverWorker(RoverId(1), cordDefault, West)
    rover.oneStepBack shouldBe RoverWorker(RoverId(1), Cord(1, 0), West)
  }

  it should "go forward when direction is North" in {
    val rover = RoverWorker(RoverId(1), cordDefault, North)
    rover.move(MoveForward) shouldBe RoverWorker(RoverId(1), Cord(0, 1), North)
  }

  it should "go forward when direction is East" in {
    val rover = RoverWorker(RoverId(1), cordDefault, East)
    rover.move(MoveForward) shouldBe RoverWorker(RoverId(1), Cord(1, 0), East)
  }

  it should "go forward when direction is South" in {
    val rover = RoverWorker(RoverId(1), cordDefault, South)
    rover.move(MoveForward) shouldBe RoverWorker(RoverId(1), Cord(0, -1), South)
  }

  it should "go forward when direction is West" in {
    val rover = RoverWorker(RoverId(1), cordDefault, West)
    rover.move(MoveForward) shouldBe RoverWorker(RoverId(1), Cord(-1, 0), West)
  }

  it should "turn right when direction North" in {
    val rover = RoverWorker(RoverId(1), cordDefault, North)
    rover.move(TurnRight) shouldBe rover.copy(heading = East)
  }

  it should "turn right when direction East" in {
    val rover = RoverWorker(RoverId(1), cordDefault, East)
    rover.move(TurnRight) shouldBe rover.copy(heading = South)
  }

  it should "turn right when direction South" in {
    val rover = RoverWorker(RoverId(1), cordDefault, South)
    rover.move(TurnRight) shouldBe rover.copy(heading = West)
  }

  it should "turn right when direction West" in {
    val rover = RoverWorker(RoverId(1), cordDefault, West)
    rover.move(TurnRight) shouldBe rover.copy(heading = North)
  }

  it should "turn left when direction North" in {
    val rover = RoverWorker(RoverId(1), cordDefault, North)
    rover.move(TurnRight) shouldBe rover.copy(heading = East)
  }

  it should "turn left when direction East" in {
    val rover = RoverWorker(RoverId(1), cordDefault, East)
    rover.move(TurnRight) shouldBe rover.copy(heading = South)
  }

  it should "turn left when direction South" in {
    val rover = RoverWorker(RoverId(1), cordDefault, South)
    rover.move(TurnRight) shouldBe rover.copy(heading = West)
  }

  it should "turn left when direction West" in {
    val rover = RoverWorker(RoverId(1), cordDefault, West)
    rover.move(TurnRight) shouldBe rover.copy(heading = North)
  }

  it should "do nothing in unknown command" in {
    val rover = RoverWorker(RoverId(1), cordDefault, West)
    rover.move(Unknown) shouldBe rover
  }
}
