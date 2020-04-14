package streams

import java.util.concurrent.TimeUnit

import scala.{ Stream => _ }

import zio.clock.Clock
import zio.console.putStrLn
import zio.duration.Duration
import zio.stream._
import zio.{ Runtime, URIO, ZIO }

trait Common {
  val e = new RuntimeException("boom")

  val compute: URIO[Clock, Unit] = ZIO.sleep(Duration(5, TimeUnit.SECONDS))

  val failM: ZIO[Any, String, Nothing]               = ZIO.fail("Failed")
  val failS: ZStream[Any, RuntimeException, Nothing] = ZStream.fail(e)
}

case class Channel(id: Int)   extends Common
case class Followers(id: Int) extends Common

case class User(ch: Seq[Channel], flw: Seq[Followers])

class Manager() {
  def blockUser(user: User): Unit = ???

  def readPass[A](users: Stream[Throwable, A])  = ???
  def blockPass[A](users: Stream[Throwable, A]) = ???
}

object App1 extends App {
  val rt = Runtime.default

  val N = 1
  val M = 1
  val P = 2

  val channels  = (1 to N).map(Channel)
  val followers = (1 to M).map(Followers)
  val users     = List.fill(P)(User(channels, followers))

  val app = for {
    userStream <- ZStream.fromIterable(users).runCollect
    _          <- putStrLn(userStream.toString)
  } yield ()

  rt.unsafeRun(app)
}
