package ru.vtblife.classified.ska.configuration

import zio.RIO
import zio.Task
import pureconfig.ConfigSource

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
        Task.effectTotal(Config(ApiConfig("localhost", 8080), ServiceAConfig("a"), ServiceBConfig("b")))
    }
  }

  object Test extends Test
}
