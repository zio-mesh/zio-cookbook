package streams
import Helper._

import zio.console.putStrLn
import zio.stream.ZStream
import zio.{ App, Promise }

object App2 extends App {
  def run(args: List[String]) =
    // app.fold(_ => 1, _ => 0)
    app.as(0)

  val outerStream = ZStream.fromEffect(doWork(followers))

  val app = for {
    latch       <- Promise.make[Nothing, Unit]
    innerStream = ZStream.fromEffect(latch.succeed(()))

    followers <- outerStream.drainFork(innerStream).runDrain
    _         <- putStrLn(followers.toString)

  } yield ()

}
