package ru.vtblife.classified.ska.external

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits._
import org.http4s.{EntityDecoder, EntityEncoder}

trait ServiceB[F[_]] {
  def get(id: String): F[ServiceB.ServiceBData]
}

object ServiceB {
  def apply[F[_]](implicit ev: ServiceB[F]): ServiceB[F] = ev

  final case class ServiceBData(joke: String) extends AnyVal

  object ServiceBData {
    implicit val bDecoder: Decoder[ServiceBData] = deriveDecoder[ServiceBData]
    implicit def bEntityDecoder[F[_]: Sync]: EntityDecoder[F, ServiceBData] =
      jsonOf
    implicit val bEncoder: Encoder[ServiceBData] = deriveEncoder[ServiceBData]
    implicit def bEntityEncoder[F[_]: Applicative]: EntityEncoder[F, ServiceBData] =
      jsonEncoderOf
  }

  final case class ServiceBError(e: Throwable) extends RuntimeException

  def impl[F[_]: Sync](C: Client[F]): ServiceB[F] = new ServiceB[F] {
    val dsl: Http4sClientDsl[F] = new Http4sClientDsl[F] {}
    import dsl._

    def get(id: String): F[ServiceB.ServiceBData] =
      C.expect[ServiceBData](GET(uri"https://icanhazdadjoke.com/"))
        .adaptError { case t => ServiceBError(t) } // Prevent Client Json Decoding Failure Leaking
  }
}