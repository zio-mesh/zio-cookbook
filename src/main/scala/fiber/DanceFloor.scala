package fiber

import zio.{ ZIO }
import fiber.simplesemaphore.{ S }

import zio.duration.Duration
import zio.clock._
import zio.console._
import zio.random._

object Party {
  val party = for {
    dancefloor <- S(10)
    dancers <- ZIO.foreachPar(1 to 100) { i =>
                dancefloor.P *> nextDouble.map(d => Duration.fromNanos((d * 1000000).round)).flatMap { d =>
                  putStrLn(s"${i} checking my boots") *> sleep(d) *> putStrLn(s"${i} dancing like it's 99")
                } *> dancefloor.V
              }
  } yield true

}
