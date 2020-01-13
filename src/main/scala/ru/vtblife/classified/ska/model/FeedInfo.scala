package ru.vtblife.classified.ska.model

import cats.Applicative
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import ru.vtblife.classified.ska.external.ServiceA.ServiceAData
import ru.vtblife.classified.ska.external.ServiceB.ServiceBData

final case class FeedInfo(a: ServiceAData, b: ServiceBData)

object FeedInfo {
  implicit val feedInfoEncoder: Encoder[FeedInfo] = deriveEncoder[FeedInfo]
  implicit def feedInfoEntityEncoder[F[_]: Applicative]: EntityEncoder[F, FeedInfo] =
    jsonEncoderOf
}
