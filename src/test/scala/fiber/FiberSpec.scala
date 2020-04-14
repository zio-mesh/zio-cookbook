package fiber

import Fib0._

import zio._
import zio.test.Assertion._
import zio.test._

object Fib0 {
  def getIO    = IO.unit
  def getFiber = Fiber.unit

}

object Fib0Spec extends DefaultRunnableSpec {
  def spec = suite("Fib0Spec")(
    testM("Fib0 works") {
      for {
        fib <- getIO.fork
        out <- fib.await
      } yield assert(out)(equalTo(Exit.unit))
    }
  )
}
