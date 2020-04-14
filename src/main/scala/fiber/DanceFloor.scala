package zio.cookbook.fiber

import zio.clock._
import zio.console._
import zio.cookbook.fiber.simplesemaphore._
import zio.duration.Duration
import zio.random._
import zio.{ ZIO }

object Party {
  val fibers    = 4
  val dancers   = 10L
  val danceTime = 100000L

  val party = for {
    dancefloor <- S(dancers)
    _ <- ZIO.foreachPar(1 to fibers) { i =>
          dancefloor.P *> // decrease semaphore counter
            // The block below represents a Monadic semantics and runs sequentially in a single fiber
            nextDouble.map(d => Duration.fromNanos((d * 100000).round)).flatMap { d =>
              putStrLn(s"${i} checking my boots") *>   // effect 1
                sleep(d) *>                            // effect 2
                putStrLn(s"${i} dancing like it's 99") // effect 3
            } *>
            dancefloor.V // increase semaphore counter
        }
  } yield true

}
