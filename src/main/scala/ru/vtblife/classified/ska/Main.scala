package ru.vtblife.classified.ska

import zio.Task
import zio.interop.catz._
import zio.interop.catz.implicits._

object Main extends CatsApp {
  override def run(args: List[String]) =
    Server.run[Task].fold(_ => 1, _ => 0)
//    Server.stream[Task].compile[Task, Task, Nothing].drain.fold(_ => 1, _ => 0)
//    Server.stream[IO].compile.drain.as(ExitCode.Success)
}