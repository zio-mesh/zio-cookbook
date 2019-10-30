package env

// import zio._
import zio.test.{ assert, suite, test, DefaultRunnableSpec }
import zio.test.Assertion.{ equalTo }

object DBSpec extends DefaultRunnableSpec(suite("DBSpec")(test("works") { assert(0, equalTo(0)) }))
