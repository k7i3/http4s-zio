package ru.vtblife.classified.ska

import cats.effect.ConcurrentEffect
import cats.effect.ContextShift
import cats.effect.ExitCode
import cats.effect.Timer
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import ru.vtblife.classified.ska.configuration.Config
import ru.vtblife.classified.ska.external.ServiceA
import ru.vtblife.classified.ska.external.ServiceB
import zio.Task

import scala.concurrent.ExecutionContext.global

object Services {

  def client(
      config: Config
    )(
      implicit C: ConcurrentEffect[Task]
    ): Stream[Task, (ServiceA[Task], ServiceB[Task])] = {
    for {
      client <- BlazeClientBuilder[Task](global).stream // todo grpc
    } yield (
      ServiceA.impl[Task](config.serviceAConfig)(client),
      ServiceB.impl[Task](config.serviceBConfig)(client)
    )
  }

  def server(
      config: Config
    )(
      serviceA: ServiceA[Task],
      serviceB: ServiceB[Task]
    )(
      implicit T: Timer[Task],
      C: ConcurrentEffect[Task]
    ): Stream[Task, ExitCode] = {

    val httpApp = Router[Task](
      "/v1" -> Routes.infoRoutes[Task](serviceA, serviceB)
    ).orNotFound

    val finalHttpApp = Logger.httpApp(true, true)(httpApp)

    for {
      exitCode <- BlazeServerBuilder[Task]
        .bindHttp(config.apiConfig.port, config.apiConfig.endpoint)
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }

  def cs[F[_]: ConcurrentEffect](
      config: Config
    )(
      implicit T: Timer[F],
      C: ContextShift[F]
    ): Stream[F, ExitCode] = {
    for {
      client <- BlazeClientBuilder[F](global).stream
      serviceA = ServiceA.impl[F](config.serviceAConfig)(client)
      serviceB = ServiceB.impl[F](config.serviceBConfig)(client)

      httpApp = Router[F](
        "/v1" -> Routes.infoRoutes[F](serviceA, serviceB)
      ).orNotFound

      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      exitCode <- BlazeServerBuilder[F]
        .bindHttp(config.apiConfig.port, config.apiConfig.endpoint)
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }
}
