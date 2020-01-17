package ru.vtblife.classified.ska

import cats.effect.ConcurrentEffect
import cats.effect.ExitCode
import cats.effect.Sync
import cats.effect.Timer
import fs2.Stream
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import ru.vtblife.classified.ska.configuration.Config
import ru.vtblife.classified.ska.external.ServiceA
import ru.vtblife.classified.ska.external.ServiceB
import scala.concurrent.ExecutionContext.global
import org.http4s.client.Client
import org.lyranthe.fs2_grpc.java_runtime.implicits._

object Services {

  def grpcClient[F[_]: Sync](name: String, port: Int): Stream[F, ManagedChannel] =
    ManagedChannelBuilder
      .forAddress(name, port)
      .stream[F]

  def httpClient[F[_]: ConcurrentEffect]: Stream[F, Client[F]] =
    BlazeClientBuilder[F](global).stream

  def client[F[_]: ConcurrentEffect](config: Config): Stream[F, (ServiceA[F], ServiceB[F])] = {
    for {
      grpc <- grpcClient[F](config.serviceBConfig.grpc.name, config.serviceBConfig.grpc.port)
      http <- httpClient[F]
    } yield (
      ServiceA.impl[F](config.serviceAConfig)(http),
      ServiceB.impl[F](config.serviceBConfig)(grpc)
    )
  }

  def server[F[_]: ConcurrentEffect: Timer](
      config: Config
    )(
      serviceA: ServiceA[F],
      serviceB: ServiceB[F]
    ): Stream[F, ExitCode] = {

    val httpApp = Router[F](
      "/v1" -> Routes.infoRoutes[F](serviceA, serviceB)
    ).orNotFound

    val finalHttpApp = Logger.httpApp(true, true)(httpApp)

    for {
      exitCode <- BlazeServerBuilder[F]
        .bindHttp(config.apiConfig.port, config.apiConfig.endpoint)
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }

  def cs[F[_]: ConcurrentEffect](config: Config)(implicit T: Timer[F]): Stream[F, ExitCode] = {
    for {
      http <- BlazeClientBuilder[F](global).stream
      grpc <- grpcClient[F](config.serviceBConfig.grpc.name, config.serviceBConfig.grpc.port)
      serviceA = ServiceA.impl[F](config.serviceAConfig)(http)
      serviceB = ServiceB.impl[F](config.serviceBConfig)(grpc)

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
