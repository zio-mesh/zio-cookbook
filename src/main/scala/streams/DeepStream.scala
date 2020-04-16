package streams

import Helper._

import zio.clock.{ sleep }
import zio.console.putStrLn
import zio.duration._
import zio.{ App, Promise }

object App1 extends App {
  def run(args: List[String]) =
    // app.fold(_ => 1, _ => 0)
    app.as(0)

  val app = for {
    latch <- Promise.make[Nothing, Unit]

    // Process Channels in a Separate fiber, sleep 1 second and set the latch
    _ <- (doWork(channels).fork <* sleep(1.second)).flatMap(_ => latch.succeed(()))

    // Followers are processed in own fiber, which starts only after channels finish
    followersOut <- doWork(followers).fork.ensuring(latch.succeed(()))

    // Get followers result from its fiber
    followers <- followersOut.join
    _         <- putStrLn(followers.toString)
  } yield ()

}
