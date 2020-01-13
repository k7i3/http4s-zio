package ru.vtblife.classified.ska

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import fs2.Stream
import org.http4s.server.Router
import ru.vtblife.classified.ska.external.{ServiceA, ServiceB}

import scala.concurrent.ExecutionContext.global

object Server {
  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {
    for {
      client <- BlazeClientBuilder[F](global).stream // todo grpc
      serviceA = ServiceA.impl[F](client)
      serviceB = ServiceB.impl[F](client)

      httpApp = Router[F](
        "/v1" -> Routes.infoRoutes[F](serviceA, serviceB)
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      exitCode <- BlazeServerBuilder[F]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}