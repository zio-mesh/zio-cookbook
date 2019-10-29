package fiber

import zio.test.{ assert, suite, testM, DefaultRunnableSpec }
import zio.test.Assertion.{ isTrue }

object DanceFloorSpec
    extends DefaultRunnableSpec(
      suite("DanceFloorSpec")(
        testM("sayHello correctly displays output") {
          for {
            out <- Party.party
          } yield assert(out, isTrue)
        }
      )
    )
