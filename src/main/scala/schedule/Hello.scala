package schedule

import Common._

import zio.blocking.blocking
import zio.console.putStrLn
import zio.{ App, ZIO }

object App0 extends App {
  def run(args: List[String]) =
    app.fold(_ => 1, _ => 0)

  val app = for {
    res0 <- eff0.repeat(simpleSched)
    res1 <- eff1.repeat(timedSched)
    _    <- putStrLn(res0.toString)
    _    <- putStrLn(res1.toString)
  } yield ()
}

object App1 extends App {
  def run(args: List[String]) =
    app3.fold(_ => 1, _ => 0)

  val app0 = blocking(eff0.retry(simpleSched).tap(msg => putStrLn(msg.toString)))
  val app1 = blocking(eff1.retry(timedSched).tap(msg => putStrLn(msg.toString)))
  val app2 = blocking(eff2.retry(simpleSched).tap(msg => putStrLn(msg.toString)))
  val app3 = blocking(eff3.retry(timedSched).tap(msg => putStrLn(msg.toString)))
  // val app0 = for {
  //   fiber0 <- blocking(eff0.retry(simpleSched).tap(msg => putStrLn(msg.toString))).fork
  //   fiber1 <- blocking(eff1.retry(timedSched).tap(msg => putStrLn(msg.toString))).fork
  //   fiber2 <- blocking(eff2.retry(simpleSched).tap(msg => putStrLn(msg.toString))).fork
  //   fiber3 <- blocking(eff3.retry(timedSched).tap(msg => putStrLn(msg.toString))).fork
  //   _      <- fiber0.join
  //   _      <- fiber1.join
  //   _      <- fiber2.join
  //   _      <- fiber3.join
  // } yield ()

}
