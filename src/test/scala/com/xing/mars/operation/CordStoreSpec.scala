package com.xing.mars.operation

import java.util.concurrent.ConcurrentHashMap

import cats.effect.IO
import com.xing.mars.operation.store.PlateauState
import org.scalatest.{Matchers, Outcome, fixture}
import cats.syntax.option._
import com.xing.mars.operation.RoverWorker.RoverId
import com.xing.mars.operation.Status._
import com.xing.mars.operation.store.PlateauState.UpdateResult.{Failure, Success}

/**
  * @author Mikhail Nemenko { @literal <nemenkoma@gmail.com>}
  */
class CordStoreSpec extends fixture.FlatSpec with Matchers {

  final case class FixtureParam(store: PlateauState[IO])

  val cord: Cord = Cord(0, 0)
  val fieldStatus: FieldStatus = FieldStatus.default

  override protected def withFixture(test: OneArgTest): Outcome = {
    val empty = new ConcurrentHashMap[Cord, FieldStatus]()
    val store = PlateauState[IO](empty)
    val fixture = FixtureParam(store)
    super.withFixture(test.toNoArgTest(fixture))
  }

  it should "return nothing when store is empty" in { f =>
    f.store.getAll.unsafeRunSync() shouldBe empty
  }

  it should "return none when key is not present" in { f =>
    f.store.get(cord).unsafeRunSync() shouldBe empty
  }

  it should "put one element in store" in { f =>
    (for {
      before <- f.store.get(cord)
      _ <- f.store.put(cord, fieldStatus)
      after <- f.store.get(cord)
    } yield {
      before shouldBe empty
      after shouldBe fieldStatus.some
    }).unsafeRunSync()
  }

  it should "rewrite element in store" in { f =>
    val newFieldStatus = FieldStatus(RoverId(Integer.MAX_VALUE).some, Finish)

    (for {
      before <- f.store.get(cord)
      _ <- f.store.put(cord, fieldStatus)
      afterFirstPut <- f.store.get(cord)
      _ <- f.store.put(cord, newFieldStatus)
      afterSecondPut <- f.store.get(cord)
    } yield {
      before shouldBe empty
      afterFirstPut shouldBe fieldStatus.some
      afterSecondPut shouldBe newFieldStatus.some
    }).unsafeRunSync()
  }

  it should "release field" in { f =>
    val finish = FieldStatus(RoverId(Integer.MAX_VALUE).some, Finish)

    (for {
      _ <- f.store.put(cord, finish)
      _ <- f.store.releasePrev(cord)
      afterRelease <- f.store.get(cord)
    } yield afterRelease shouldBe fieldStatus.some).unsafeRunSync()
  }

  it should "putAll elements in store" in { f =>
    val map: Map[Cord, FieldStatus] = Map(
      Cord(0, 0) -> FieldStatus(none[RoverId], Free),
      Cord(0, 1) -> FieldStatus(RoverId(1).some, Finish),
      Cord(0, 2) -> FieldStatus(RoverId(2).some, Waiting),
      Cord(0, 3) -> FieldStatus(RoverId(3).some, Finish)
    )
    for {
      _ <- f.store.putAll(map)
      all <- f.store.getAll
    } yield all should contain theSameElementsAs map.toList
  }

  it should "return OutOfPlateau" in { f =>
    val cord = Cord(Int.MaxValue, Int.MaxValue)
    (for {
      updateStatus <- f.store.tryUpdate(cord, FieldStatus(RoverId(1).some, Waiting))
    } yield {
      updateStatus shouldBe Failure(FieldStatus(None, OutOfPlateau))
    }).unsafeRunSync()
  }

  it should "return new field waiting status if field is free" in { f =>
    val fieldStatus = FieldStatus(RoverId(1).some, Waiting)
    (for {
      _ <- f.store.put(cord, fieldStatus)
      update <- f.store.tryUpdate(cord, fieldStatus)
      get <- f.store.get(cord)
    } yield update.some shouldBe get.map(Success.apply)).unsafeRunSync()
  }

  it should "return new field finish status if field is free" in { f =>
    val fieldStatus = FieldStatus(RoverId(1).some, Finish)
    (for {
      _ <- f.store.put(cord, fieldStatus)
      update <- f.store.tryUpdate(cord, fieldStatus)
      get <- f.store.get(cord)
    } yield update.some shouldBe get.map(Success.apply)).unsafeRunSync()
  }

  it should "be updated in waiting status if you are owner" in { f =>
    val fieldStatus = FieldStatus(RoverId(1000).some, Waiting)
    (for {
      _ <- f.store.put(cord, fieldStatus)
      update <- f.store.tryUpdate(cord, fieldStatus.copy(status = Finish))
      get <- f.store.get(cord)
    } yield update.some shouldBe get.map(Success.apply)).unsafeRunSync()
  }

  it should "not be updated in waiting status if you are not owner" in { f =>
    val fieldStatus = FieldStatus(RoverId(1000).some, Waiting)
    (for {
      _ <- f.store.put(cord, fieldStatus)
      update <- f.store.tryUpdate(cord, FieldStatus(RoverId(1).some, Finish))
      get <- f.store.get(cord)
    } yield {
      update.some shouldBe get.map(Failure.apply)
      update should not be FieldStatus(RoverId(1).some, Finish)
      get shouldBe fieldStatus.some
    }).unsafeRunSync()
  }

  it should "not change finish status" in { f =>
    val fieldStatus = FieldStatus(RoverId(1000).some, Finish)
    (for {
      _ <- f.store.put(cord, fieldStatus)
      first <- f.store.tryUpdate(cord, FieldStatus.default)
      getAfterFirst <- f.store.get(cord)
      second <- f.store.tryUpdate(cord, fieldStatus.copy(status = Free))
      getAfterSecond <- f.store.get(cord)
    } yield {
      getAfterFirst shouldBe fieldStatus.some
      getAfterSecond shouldBe fieldStatus.some
      first shouldBe Failure(fieldStatus)
      second shouldBe second
    }).unsafeRunSync()
  }
}
