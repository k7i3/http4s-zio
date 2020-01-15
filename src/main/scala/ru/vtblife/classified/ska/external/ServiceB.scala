package ru.vtblife.classified.ska.external

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto._
import org.http4s.Method._
import org.http4s.EntityDecoder
import org.http4s.EntityEncoder
import org.http4s.Uri
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import ru.vtblife.classified.ska.configuration.ServiceBConfig

trait ServiceB[F[_]] {
  val uri: Uri
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

  def impl[F[_]: Sync](config: ServiceBConfig)(C: Client[F]): ServiceB[F] = new ServiceB[F] {
    val uri: Uri                = Uri.unsafeFromString(config.url)
    val dsl: Http4sClientDsl[F] = new Http4sClientDsl[F] {}
    import dsl._

    def get(id: String): F[ServiceB.ServiceBData] =
      C.expect[ServiceBData](GET(uri)) // uri"${config.url}"
        .adaptError { case t => ServiceBError(t) } // Prevent Client Json Decoding Failure Leaking
  }
}
