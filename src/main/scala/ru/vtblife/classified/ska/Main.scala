package ru.vtblife.classified.ska

import ru.vtblife.classified.ska.configuration.Configuration
import zio.interop.catz._
import zio.interop.catz.implicits._
import zio.Task
import zio.ZEnv
import zio.ZIO

object Main extends CatsApp {

  val app: ZIO[Any, Throwable, Unit] = for {
    config <- configuration.loadConfig.provide(Configuration.Live)
    _ <- Services
      .client(config)
      .flatMap {
        case (a, b) => Services.server(config)(a, b)
      }
      .compile
      .drain
  } yield ()

  val cs: ZIO[Any, Throwable, Unit] = for {
    config <- configuration.loadConfig.provide(Configuration.Live)
    _      <- Services.cs[Task](config).compile.drain
  } yield ()

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    app.fold(_ => 1, _ => 0)
//    cs.fold(_ => 1, _ => 0)
//    Server.stream[Task].compile[Task, Task, Nothing].drain.fold(_ => 1, _ => 0)
//    Server.stream[IO].compile.drain.as(ExitCode.Success)
}
