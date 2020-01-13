package ru.vtblife.classified.ska

import zio.RIO

package object configuration {

  case class Config(apiConfig: ApiConfig, serviceAConfig: ServiceAConfig, serviceBConfig: ServiceBConfig)
  case class ApiConfig(endpoint: String, port: Int)
  case class ServiceAConfig(url: String)
  case class ServiceBConfig(url: String)

  def loadConfig: RIO[Configuration, Config] = RIO.accessM(_.config.load)
}
