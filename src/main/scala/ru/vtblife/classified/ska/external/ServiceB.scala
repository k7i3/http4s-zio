package ru.vtblife.classified.ska.external

import cats.effect.ConcurrentEffect
import com.example.protos.hello.GreeterFs2Grpc
import com.example.protos.hello.HelloReply
import com.example.protos.hello.HelloRequest
import io.grpc.ManagedChannel
import io.grpc.Metadata
import ru.vtblife.classified.ska.configuration.ServiceBConfig
import cats.implicits._

trait ServiceB[F[_]] {
  def get(id: String): F[HelloReply]
}

object ServiceB {
  def apply[F[_]](implicit ev: ServiceB[F]): ServiceB[F] = ev

  final case class ServiceBError(e: Throwable) extends RuntimeException

  def impl[F[_]: ConcurrentEffect](config: ServiceBConfig)(grpc: ManagedChannel): ServiceB[F] =
    new ServiceB[F] {
      val greater: GreeterFs2Grpc[F, Metadata] = GreeterFs2Grpc.stub[F](grpc)

      def get(id: String): F[HelloReply] =
        greater
          .sayHello(HelloRequest("k7i3"), new Metadata())
          .adaptError {
            case t => ServiceBError(t)
          }
          .recover {
            case ServiceBError(_) => HelloReply("ServiceBError")
            case _                => HelloReply("Error")
          }
    }
}
