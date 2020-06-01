package streams

import zio.clock.Clock
import zio.console.Console
import zio.console.putStrLn
import zio.duration._
import zio.stream.{ Stream }
import zio.{ App, Schedule }

object App5 extends App {
  def run(args: List[String]) = app.exitCode

  val s1    = Stream(1, 2) ++ Stream.fail("Boom")
  val s2    = Stream(3, 4)
  val sched = Schedule.exponential(1.millisecond) <* Schedule.recurs(5)

  val app = for {
    out <- s1.catchAllCause(_ => s2).schedule(sched).runCollect
    _   <- putStrLn(out.toString)
  } yield ()
}

object App6 extends App {
  def run(args: List[String]) = app.exitCode

  val succ  = (Timed.longSuccess(3) <* putStrLn("Tap")).provideLayer(Clock.live ++ Console.live)
  val fail  = Timed.longFail.provideLayer(Clock.live)
  val fatal = Timed.longExcept.provideLayer(Clock.live)

  val s0 = Stream(0, 1) ++ Stream.fromEffect(succ)
  val s1 = Stream(2, 3) ++ Stream.fromEffect(fail)
  val s2 = Stream(3, 4) ++ Stream.fromEffect(fatal)
  val s3 = Stream(5, 6) ++ Stream.fromEffect(succ)

  val sched = Schedule.exponential(1.second) <* Schedule.recurs(5)

  val app = for {
    r0 <- s0.schedule(sched).runCollect
    // r1 <- s0.runCollect
    _ <- putStrLn(r0.toString)
  } yield ()

}
