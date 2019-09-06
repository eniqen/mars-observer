package com.xing.mars.operation.journal

import java.io.{FileWriter, InputStream, PrintWriter}

import cats.effect.{Resource, Sync}
import cats.syntax.functor._
import com.xing.mars.operation.RoverWorker.RoverId
import com.xing.mars.operation.{Cord, RoverWorker}
import com.xing.mars.operation.actions.Command.{
  MoveForward,
  TurnLeft,
  TurnRight,
  Unknown
}
import com.xing.mars.operation.actions.{Command, Direction}
import com.xing.mars.operation.journal.Journal.{Log, Logs}

import scala.io.Source

/**
  * @author Mikhail Nemenko { @literal <nemenkoma@gmail.com>}
  */
class FileJournal[F[_]: Sync](inPath: String, outPath: String)
    extends Journal[F] {
  override def readLogs: F[Journal.Logs] = {
    val acquire = Sync[F].delay(this.getClass.getResourceAsStream(inPath))
    val release: InputStream => F[Unit] = is => Sync[F].delay(is.close())

    Resource.make(acquire)(release).use { is =>
      Sync[F]
        .delay(Source.fromInputStream(is).getLines().toList)
        .map(mapStringToLog)
    }
  }

  private def mapStringToLog(lines: List[String]): Logs = {
    val Array(x, y) = lines.head.split(" ")
    val logs = lines.tail
      .grouped(2)
      .zipWithIndex
      .foldLeft(Logs(to = Cord(x.toInt, y.toInt), list = List.empty)) {
        case (logs, (List(workerInfo, commands), id)) =>
          val log = Log(parseRoverInfo(workerInfo, id), parseCommands(commands))
          logs.copy(list = log :: logs.list)
      }

    logs.copy(list = logs.list.reverse)
  }

  private def parseCommands(commands: String): List[Command] =
    commands.trim
      .map {
        case 'M' => MoveForward
        case 'L' => TurnLeft
        case 'R' => TurnRight
        case ch => {
          println("Unknown command " + ch)
          Command.Unknown
        }
      }
      .filterNot(_ == Unknown)
      .toList

  private def parseRoverInfo(info: String, id: Int): RoverWorker =
    info.trim.split(" ") match {
      case Array(x, y, direction) =>
        RoverWorker(RoverId(id),
                    Cord(x.toInt, y.toInt),
                    Direction.withName(direction))
    }

  override def sendReport(report: Journal.Report): F[Unit] = {
    val alloc = Sync[F].delay(new PrintWriter(new FileWriter(outPath, true)))

    Resource.fromAutoCloseable(alloc).use { writer =>
      Sync[F].delay(
        writer.write(
          s"${report.cord.x} ${report.cord.y} ${report.direction.entryName}\n"))
    }
  }
}
