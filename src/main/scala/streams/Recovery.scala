package streams

import zio.console.putStrLn
import zio.duration._
import zio.stream.{ Stream }
import zio.{ App, Schedule }

object App4 extends App {
  def run(args: List[String]) =
    app.as(0)

  val s1    = Stream(1, 2) ++ Stream.fail("Boom")
  val s2    = Stream(3, 4)
  val sched = Schedule.exponential(1.millisecond) <* Schedule.recurs(5)

  val app = for {
    out <- s1.catchAllCause(_ => s2).schedule(sched).runCollect
    _   <- putStrLn(out.toString)
  } yield ()
}
