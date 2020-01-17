package ru.vtblife.classified.ska

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import ru.vtblife.classified.ska.external.ServiceA
import ru.vtblife.classified.ska.external.ServiceB
import ru.vtblife.classified.ska.model.FeedInfo

object Routes {

  def infoRoutes[F[_]: Sync](A: ServiceA[F], B: ServiceB[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "feeds" / id =>
        for {
          aData <- A.get(id)
          bData <- B.get(id)
          feedInfo = FeedInfo(aData, bData.message)
          resp <- Ok(feedInfo)
        } yield resp
    }
  }

  def helloWorldRoutes[F[_]: Sync](H: HelloWorld[F]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        for {
          greeting <- H.hello(HelloWorld.Name(name))
          resp     <- Ok(greeting)
        } yield resp
    }
  }
}
