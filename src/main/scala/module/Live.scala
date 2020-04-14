package module

import zio.Task
import pureconfig.ConfigSource
import pureconfig.generic.auto._

trait Live extends Configuration {
  val config = new Configuration.Service[Any] {
    val load: Task[Config] = Task.effect(ConfigSource.default.loadOrThrow[Config])
  }
}

trait Test extends Configuration {
  val config = new Configuration.Service[Any] {
    val load: Task[Config] = Task.effectTotal(Config(new ApiConfig(), new DbConfig()))
  }
}
