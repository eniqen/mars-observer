package com.xing.mars.operation

import com.xing.mars.operation.actions.Direction.{East, North, South, West}
import org.scalatest.{FreeSpec, Matchers}

/**
  * @author Mikhail Nemenko { @literal <nemenkoma@gmail.com>}
  */
class DirectionOpsSpec extends FreeSpec with Matchers {
  "HeadingSpecOps" - {
    "After turn left" - {
      "North should change direction West" - {
        North.turnLeft shouldBe West
      }
      "West should change direction South" - {
        West.turnLeft shouldBe South
      }
      "South should change direction East" - {
        South.turnLeft shouldBe East
      }
      "East should change direction North" - {
        East.turnLeft shouldBe North
      }
    }
    "After turn right" - {
      "North should change direction East" - {
        North.turnRight shouldBe East
      }
      "East should change direction South" - {
        East.turnRight shouldBe South
      }
      "South should change direction West" - {
        South.turnRight shouldBe West
      }
      "West should change direction North" - {
        West.turnRight shouldBe North
      }
    }

    "North After 360 degrees" - {
      "turnRight should have the direction" - {
        North.turnRight.turnRight.turnRight.turnRight shouldBe North
      }
      "turnLeft should have the direction" - {
        North.turnLeft.turnLeft.turnLeft.turnLeft shouldBe North
      }
    }
  }
}
