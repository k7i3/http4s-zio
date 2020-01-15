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
import ru.vtblife.classified.ska.configuration.ServiceAConfig

trait ServiceA[F[_]] {
  def get(id: String): F[ServiceA.ServiceAData]
}

object ServiceA {
  def apply[F[_]](implicit ev: ServiceA[F]): ServiceA[F] = ev

  final case class ServiceAData(joke: String) extends AnyVal

  object ServiceAData {
    implicit val aDecoder: Decoder[ServiceAData] = deriveDecoder[ServiceAData]
    implicit def aEntityDecoder[F[_]: Sync]: EntityDecoder[F, ServiceAData] =
      jsonOf
    implicit val aEncoder: Encoder[ServiceAData] = deriveEncoder[ServiceAData]
    implicit def aEntityEncoder[F[_]: Applicative]: EntityEncoder[F, ServiceAData] =
      jsonEncoderOf
  }

  final case class ServiceAError(e: Throwable) extends RuntimeException

  def impl[F[_]: Sync](config: ServiceAConfig)(C: Client[F]): ServiceA[F] = new ServiceA[F] {
    val uri: Uri                = Uri.unsafeFromString(config.url)
    val dsl: Http4sClientDsl[F] = new Http4sClientDsl[F] {}
    import dsl._

    def get(id: String): F[ServiceA.ServiceAData] =
      C.expect[ServiceAData](GET(uri)) // uri"${config.url}"
        .adaptError { case t => ServiceAError(t) } // Prevent Client Json Decoding Failure Leaking
  }
}
