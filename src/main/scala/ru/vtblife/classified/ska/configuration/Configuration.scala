package ru.vtblife.classified.ska.configuration

import pureconfig.ConfigSource
import zio.RIO
import zio.Task

trait Configuration {
  val config: Configuration.Service[Any]
}

object Configuration {

  trait Service[R] {
    val load: RIO[R, Config]
  }

  trait Live extends Configuration {

    val config: Service[Any] = new Service[Any] {
      import pureconfig.generic.auto._

      val load: Task[Config] = Task.effect(ConfigSource.default.loadOrThrow[Config])
    }
  }

  object Live extends Live

  trait Test extends Configuration {

    val config: Service[Any] = new Service[Any] {

      val load: Task[Config] =
        Task.effectTotal(
          Config(ApiConfig("0.0.0.0", 8080), ServiceAConfig(HttpConfig("https://icanhazdadjoke.com/")), ServiceBConfig(GrpcConfig("0.0.0.0", 9002)))
        )
    }
  }

  object Test extends Test
}
