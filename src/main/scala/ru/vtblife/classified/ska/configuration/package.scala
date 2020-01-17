package ru.vtblife.classified.ska

import zio.RIO

package object configuration {

  case class Config(
      apiConfig: ApiConfig,
      serviceAConfig: ServiceAConfig,
      serviceBConfig: ServiceBConfig)
  case class ApiConfig(endpoint: String, port: Int)
  case class HttpConfig(url: String)
  case class GrpcConfig(name: String, port: Int)
  case class ServiceAConfig(http: HttpConfig)
  case class ServiceBConfig(grpc: GrpcConfig)

  def loadConfig: RIO[Configuration, Config] = RIO.accessM(_.config.load)
}
