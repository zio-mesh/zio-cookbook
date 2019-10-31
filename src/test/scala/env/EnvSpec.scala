package env

import zio.test.{ assert, suite, testM, DefaultRunnableSpec }
import zio.test.Assertion.{ equalTo }
import env.Common.UserID
import env.Common.UserProfile

object DBSpec
    extends DefaultRunnableSpec(
      suite("DBSpec")(
        testM("access to Mock DB") {
          val user0 = UserID(0)
          val prof0 = UserProfile(user0)

          for {
            _  <- TestService.setTestData(Map(user0 -> prof0))
            p0 <- TestService.lookup(user0)
          } yield assert(p0, equalTo(prof0))
        }
      )
    )
