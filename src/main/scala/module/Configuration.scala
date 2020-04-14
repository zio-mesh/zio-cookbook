package module

import zio.RIO

class ApiConfig()
class DbConfig()
final case class Config(api: ApiConfig, dbConfig: DbConfig)

trait Configuration extends Serializable {
  val config: Configuration.Service[Any]
}

object Configuration {
  trait Service[R] {
    val load: RIO[R, Config]
  }
}
