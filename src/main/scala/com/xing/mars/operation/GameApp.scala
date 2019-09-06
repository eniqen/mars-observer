package com.xing.mars.operation

import cats.Parallel
import cats.effect.{ContextShift, ExitCode, IO, IOApp, Sync, Timer}
import com.xing.mars.operation.journal.{FileJournal}
import com.xing.mars.operation.state.CommandCenter
import cats.syntax.flatMap._
import cats.syntax.functor._

/**
  * @author Mikhail Nemenko { @literal <nemenkoma@gmail.com>}
  */
object GameApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = start[IO]

  private def start[F[_]: Sync: Timer: Parallel: ContextShift]: F[ExitCode] =
    for {
      _ <- Sync[F].delay(println("Welcome to the Mars Rovers"))
      fileJournal = new FileJournal("/commands.txt",
                                    "./src/main/resources/out.txt")
      center <- CommandCenter.init(fileJournal)
      _ <- center.startExpedition
    } yield ExitCode.Success
}
