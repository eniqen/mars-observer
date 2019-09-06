package com.xing.mars.operation.state

import cats.effect.{ContextShift, Sync, Timer}
import cats.Parallel
import cats.instances.list._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import cats.syntax.parallel._
import cats.syntax.foldable._
import com.xing.mars.operation.RoverWorker.RoverId
import com.xing.mars.operation.Status.{Finish, OutOfPlateau, Waiting}
import com.xing.mars.operation._
import com.xing.mars.operation.actions.Command
import com.xing.mars.operation.journal.Journal
import com.xing.mars.operation.journal.Journal.{Log, Report}
import com.xing.mars.operation.store.PlateauState
import com.xing.mars.operation.store.PlateauState.UpdateResult.{
  Failure,
  Success
}

import scala.concurrent.duration._

/**
  * @author Mikhail Nemenko { @literal <nemenkoma@gmail.com>}
  */
class CommandCenter[F[_]: Sync: Timer: Parallel: ContextShift](
    table: PlateauState[F],
    rovers: List[Log],
    sendReport: Report => F[Unit]) {

  private def buildRoverRoute(rover: RoverWorker,
                              commands: List[Command]): List[RoverWorker] =
    Command
      .splitSteps(commands)
      .foldLeft(rover :: Nil) {
        case (acc @ last :: _, step: Step) =>
          last.simulateWork(step: _*) :: acc
      }
      .reverse

  def startExpedition: F[Unit] =
    rovers.parTraverse_(log => run(buildRoverRoute(log.rover, log.commands)))

  private def run(roverRoute: List[RoverWorker]): F[Unit] = {
    val length = roverRoute.length
    roverRoute.zipWithIndex
      .traverse_ {
        case (nextStep, index) =>
          withRetry(
            roverWorker = nextStep,
            attempt = 3,
            delay = 1.second,
            index >= length - 1,
            None
          )
      }
  }

  //fixme check try update logic rewrite using extractors
  private def withRetry(roverWorker: RoverWorker,
                        attempt: Int,
                        delay: FiniteDuration,
                        isLast: Boolean,
                        lastMet: Option[RoverId]): F[Unit] = {

    def toReport(rover: RoverWorker, status: Status): F[Report] = Sync[F].delay(
      Report(
        roverWorker.id,
        roverWorker.cord,
        roverWorker.heading,
        status
      )
    )

    val newStatus = if (isLast) Finish else Waiting
    val fieldStatus = FieldStatus(roverWorker.id.some, newStatus)
    val prev = roverWorker.oneStepBack

    table
      .tryUpdate(roverWorker.cord, fieldStatus)
      .flatMap {
        case Success(FieldStatus(_, status @ Finish)) =>
          table.releasePrev(prev.cord) >> toReport(roverWorker, status)
            .flatTap(sendReport)
            .void
        case Success(FieldStatus(_, Waiting)) =>
          table.releasePrev(prev.cord) >> toReport(roverWorker, Waiting).void
        case Failure(FieldStatus(_, OutOfPlateau)) =>
          table.put(prev.cord, FieldStatus(roverWorker.id.some, Finish)) >>
            toReport(prev, Finish).flatTap(sendReport).void
        case Failure(FieldStatus(id, Waiting)) if id == lastMet =>
          if (attempt == 0) {
            Sync[F].raiseError(new Exception("Timeout"))
          } else {
            Sync[F].delay(println(s"Try waiting field = ${roverWorker.cord}")) >>
              Timer[F].sleep(delay) >>
              withRetry(roverWorker, attempt - 1, delay * 2, isLast, id)
          }
        case Failure(FieldStatus(id, Waiting)) =>
          withRetry(roverWorker, attempt, delay, isLast, id)
      }
  }
}

case class StepException(report: Report, fieldStatus: FieldStatus)
    extends RuntimeException

object CommandCenter {
  def init[F[_]: Sync: Timer: Parallel: ContextShift](
      journal: Journal[F]): F[CommandCenter[F]] =
    for {
      logs <- journal.readLogs
      table <- generateCordStore(logs.from, logs.to)
    } yield new CommandCenter[F](table, logs.list, journal.sendReport)

  def printPlateau[F[_]: Sync](store: PlateauState[F]): F[Unit] =
    store.getAll.map(_.sortBy(x => x._1.x -> x._1.y)).map(println)

  def generateCordStore[F[_]: Sync](from: Cord = Cord(0, 0),
                                    max: Cord): F[PlateauState[F]] = {
    require(from.x >= 0 && from.y >= 0)
    require(max.x > from.x && max.y > from.y)

    val result = (for {
      x <- from.x to max.x
      y <- from.y to max.y
    } yield Cord(x, y)).foldLeft(Map.empty[Cord, FieldStatus]) {
      case (store, c) =>
        store + (c -> FieldStatus.default)
    }
    PlateauState.from(result)
  }
}
