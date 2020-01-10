package ru.vtblife.classified.ska

import cats.effect.ExitCode
import zio.{Task, URIO, ZEnv}
import zio.interop.catz._
import zio.interop.catz.implicits._

object Main extends CatsApp {
  override def run(args: List[String]): URIO[ZEnv, Int] =
    SkaServer.stream[Task].compile[Task, Task, ExitCode].drain.fold(_ => 1, _ => 0)
}