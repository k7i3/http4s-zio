package ru.vtblife.classified.ska

import ru.vtblife.classified.ska.configuration.Config
import ru.vtblife.classified.ska.configuration.Configuration
import zio.interop.catz._
import zio.interop.catz.implicits._
import zio.IO
import zio.Task
import zio.ZEnv
import zio.ZIO
import zio.console.putStrLn

object Main extends CatsApp {

  val app: ZIO[Config, Throwable, Unit] = for {
    config <- ZIO
      .environment[Config] // config <- configuration.loadConfig.provide(Configuration.Live)
    _ <- Services
      .client[Task](config)
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
    app
      .provideM(configuration.loadConfig.provide(Configuration.Live))
      .foldM(
        err => putStrLn(s"Execution failed with: $err") *> IO.succeed(1),
        _ => IO.succeed(0)
      )

//    app.provideM(configuration.loadConfig.provide(Configuration.Live)).fold(_ => 1, _ => 0)
//    cs.fold(_ => 1, _ => 0)
//    Server.stream[Task].compile[Task, Task, Nothing].drain.fold(_ => 1, _ => 0)
//    Server.stream[IO].compile.drain.as(ExitCode.Success)
}
