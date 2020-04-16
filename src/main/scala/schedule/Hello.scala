package schedule

import org.joda.time.DateTime

import zio.console.putStrLn
import zio.duration._
import zio.{ App, Schedule }

object App0 extends App {
  def run(args: List[String]) =
    app.as(0)

  def getSecondsTimestamp() = new DateTime().getSecondOfDay()

  val eff0 = putStrLn(s"Simple Tick: $getSecondsTimestamp")
  val eff1 = putStrLn(s"Timed  Tick: $getSecondsTimestamp")

  val simpleSched = Schedule.recurs(3)
  val timedSched  = Schedule.exponential(1.second) <* Schedule.recurs(3)

  val app = for {
    eff0 <- eff0.repeat(simpleSched)
    eff1 <- eff1.repeat(timedSched)
    _    <- putStrLn(eff0.toString)
    _    <- putStrLn(eff1.toString)
  } yield ()
}
