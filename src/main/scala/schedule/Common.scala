package schedule

import org.joda.time.DateTime
import zio.duration._
import zio.{ Schedule, ZIO }

object Common {
  def getSecondsTimestamp() = new DateTime().getSecondOfDay()

  val eff0 = ZIO.effect(println(s"Simple Tick: $getSecondsTimestamp"))
  val eff1 = ZIO.effect(println(s"Timed  Tick: $getSecondsTimestamp"))

  val eff2 = ZIO.fail(println(s"Simple Failed  Tick: $getSecondsTimestamp"))
  val eff3 = ZIO.fail(println(s"Timed  Failed  Tick: $getSecondsTimestamp"))

  val simpleSched = Schedule.recurs(3)
  val timedSched  = Schedule.exponential(1.second) <* Schedule.recurs(3)
}
