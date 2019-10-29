package fiber

import zio.{ App, Fiber, UIO }
import zio.console.{ putStrLn }

object Lib {
  def fib(n: Long): UIO[Long] =
    UIO {
      if (n <= 1) UIO.succeed(n)
      else fib(n - 1).zipWith(fib(n - 2))(_ + _)
    }.flatten

  val fib100Fiber: UIO[Fiber[Nothing, Long]] =
    for {
      fiber <- fib(100).fork
    } yield fiber

}

object App0 extends App {
  def run(args: List[String]) =
    compute0.fold(_ => 1, _ => 0)

  val compute0 = for {
    fibs <- Lib.fib(10).fork
    res  <- fibs.await
    _    <- putStrLn(res.toString)

  } yield (res)
}
