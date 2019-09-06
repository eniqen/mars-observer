package com.xing.mars.operation.journal

import com.xing.mars.operation.RoverWorker.RoverId
import com.xing.mars.operation.Status.Free
import com.xing.mars.operation.actions.Direction.North
import com.xing.mars.operation.actions.{Command, Direction}
import com.xing.mars.operation.journal.Journal.{Logs, Report}
import com.xing.mars.operation.{Cord, RoverWorker, Status}


/**
  * @author Mikhail Nemenko { @literal <nemenkoma@gmail.com>}
  */
trait Journal[F[_]] {
  def readLogs: F[Logs]
  def sendReport(report: Report): F[Unit]
}

object Journal {
  final case class Log(rover: RoverWorker, commands: List[Command])
  final case class Logs(from: Cord = Cord(0, 0), to: Cord, list: List[Log])

  final case class Report(id: RoverId,
                          cord: Cord,
                          direction: Direction,
                          status: Status)

  object Report {
    val empty = Report(RoverId(-1), Cord(0, 0), North, Free)
  }
}
