package ru.vtblife.classified.ska

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp {
  def run(args: List[String]) =
    SkaServer.stream[IO].compile.drain.as(ExitCode.Success)
}