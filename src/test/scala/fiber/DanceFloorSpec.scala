package zio.cookbook.fiber

import zio.duration._
import zio.test.{ assert, suite, testM, DefaultRunnableSpec }
import zio.test.Assertion.{ isTrue }
import zio.test.TestAspect.{ timeout }
import zio.test.environment.Live

object DanceFloorSpec extends DefaultRunnableSpec {
  def spec = suite("DanceFloorSpec")(
    testM("Shows how to dance with Fibers") {
      for {
        out <- Live.live(Party.party)
      } yield assert(out)(isTrue)
    } @@ timeout(15.seconds)
  )
}
