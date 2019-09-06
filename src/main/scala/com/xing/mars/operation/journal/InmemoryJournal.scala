package com.xing.mars.operation.journal

import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

import cats.effect.IO
import com.xing.mars.operation.{Cord, RoverWorker}
import com.xing.mars.operation.RoverWorker.RoverId
import com.xing.mars.operation.actions.Command.{MoveForward, TurnLeft, TurnRight}
import com.xing.mars.operation.actions.Direction.{East, North}
import com.xing.mars.operation.journal.Journal.{Log, Logs, Report}

import scala.concurrent.ExecutionContext

/**
  * @author Mikhail Nemenko { @literal <nemenkoma@gmail.com>}
  */
object InmemoryJournal extends Journal[IO] {
  private val timer = IO.timer(ExecutionContext.global)

  override def readLogs: IO[Logs] =
    IO.pure(
      Logs(
        to = Cord(5, 5),
        list = List(
          Log(
            RoverWorker(RoverId(1), Cord(1, 2), North),
            List(TurnLeft,
              MoveForward,
              TurnLeft,
              MoveForward,
              TurnLeft,
              MoveForward,
              TurnLeft,
              MoveForward,
              MoveForward)
          ),
          Log(
            RoverWorker(RoverId(2), Cord(3, 3), East),
            List(MoveForward,
              MoveForward,
              TurnRight,
              MoveForward,
              MoveForward,
              TurnRight,
              MoveForward,
              TurnRight,
              TurnRight,
              MoveForward)
          )
        )
      )
    )

  override def sendReport(report: Report): IO[Unit] = {
    timer.clock
      .realTime(TimeUnit.MILLISECONDS)
      .flatMap(now =>
        IO(println(s"""
                      |
                | REPORT #${report.id} ${getCurrentTime(now)}
                      | ------------------------
                      | $report
                      | ------------------------
                      |
      """.stripMargin)))
  }

  private def getCurrentTime(now: Long): String = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    sdf.format(new Date(now))
  }
}
