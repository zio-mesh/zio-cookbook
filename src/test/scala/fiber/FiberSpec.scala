package fiber

import zio._
import zio.test._
import zio.test.Assertion._

import Fib0._

object Fib0 {
  def getIO    = IO.unit
  def getFiber = Fiber.unit

}

object Fib0Spec
    extends DefaultRunnableSpec(
      suite("Fib0Spec")(
        testM("Fib0 works") {
          for {
            fib <- getIO.fork
            out <- fib.await
          } yield assert(out, equalTo(IO.unit))
        }
      )
    )
