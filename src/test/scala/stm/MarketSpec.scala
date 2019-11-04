package zio.cookbook.stm

// import zio.duration._
import zio.test._
import zio.test.Assertion._
// import zio.test.TestAspect.nonFlaky
import zio.stm.{ STM }

import zio.cookbook.test._
// import STMSpecUtil._

object MarketSpec
    extends ZIOBaseSpec(
      suite("STMSpec")(
        suite("Using `STM.atomically` to perform different computations and call:")(
          testM("`STM.succeed` to make a successful computation and check the value") {
            assertM(STM.succeed("Hello World").commit, equalTo("Hello World"))
          }
        )
      )
    )
