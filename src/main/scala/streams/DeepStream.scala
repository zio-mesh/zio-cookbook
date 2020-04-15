package streamsDemo

import scala.{ Stream => _ }

import Helper._

import zio.clock.{ sleep }
import zio.console.Console
import zio.console.putStrLn
import zio.duration._
import zio.stream.{ Stream, ZStream }
import zio.{ IO, Promise, Runtime, UIO, ZIO }

final case class Channel(id: Int) {
  def block(): Boolean = id % 2 == 0
}

final case class Follower(id: Int) {
  def block(): Boolean = id % 3 == 0
}

// Define Stream Workers TypeClass
trait Worker[A] {
  def procEntity(list: List[A]): ZIO[Console, Nothing, List[A]]
}
object Worker {
  implicit val chWork = new Worker[Channel] {
    def procEntity(list: List[Channel]): ZIO[zio.console.Console, Nothing, List[Channel]] =
      for {
        channelOut <- Stream
                       .fromIterable(list)
                       .mapM(item => if (item.block) IO.fail(s"Failed for $item.id") else UIO.succeed(item))
                       .buffer(bufferDepth)
                       .process
                       .use(nPulls(_, procDepth))
        filtered = channelOut.collect { case Right(value) => value }
      } yield filtered
  }

  implicit val flwWork = new Worker[Follower] {
    def procEntity(list: List[Follower]): ZIO[zio.console.Console, Nothing, List[Follower]] =
      for {
        channelOut <- Stream
                       .fromIterable(list)
                       .mapM(item => if (item.block) IO.fail(s"Failed for $item.id") else UIO.succeed(item))
                       .buffer(bufferDepth)
                       .process
                       .use(nPulls(_, procDepth))
        filtered = channelOut.collect { case Right(value) => value }
      } yield filtered
  }
}

object App1 extends App {

  val rt = Runtime.default

  val channels  = (1 to N).map(Channel).toList
  val followers = (1 to M).map(Follower).toList

  // Abstract Processor
  def doWork[A: Worker](entries: List[A]) = implicitly[Worker[A]].procEntity(entries)

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

  rt.unsafeRun(app)
}

object Helper {
  val N           = 10
  val M           = 16
  val bufferDepth = 2
  val procDepth   = 8

  def nPulls[R, E, A](pull: ZStream.Pull[R, E, A], n: Int): ZIO[R, Nothing, List[Either[Option[E], A]]] =
    ZIO.foreach(1 to n)(_ => pull.either)
}
