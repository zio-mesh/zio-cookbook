package fiber

import zio.duration._
import zio.test.{ assert, suite, testM, DefaultRunnableSpec }
import zio.test.Assertion.{ isTrue }
import zio.test.TestAspect.{ timeout }

object DanceFloorSpec
    extends DefaultRunnableSpec(
      suite("DanceFloorSpec")(
        testM("Shows how to dance with Fibers") {
          for {
            out <- Party.party
          } yield assert(out, isTrue)
        } @@ timeout(15.seconds)
      )
    )
