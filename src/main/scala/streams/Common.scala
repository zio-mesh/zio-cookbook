package streams

import java.util.concurrent.TimeUnit

import Helper._

import zio.clock.Clock
import zio.console.Console
import zio.duration.Duration
import zio.stream.{ Stream, ZStream }
import zio.{ Chunk, IO, Queue, RIO, Schedule, UIO, URIO, ZIO }

final case class Channel(id: Int) {
  def block(): Boolean = id % 2 == 0
}

final case class Follower(id: Int) {
  def block(): Boolean = id % 3 == 0
}

// Define Stream Workers TypeClass
trait Worker[A] {
  def procEntity(list: List[A]): ZIO[Console, Nothing, List[Chunk[A]]]
}
object Worker {
  implicit val chWork = new Worker[Channel] {
    def procEntity(list: List[Channel]) =
      for {
        channelOut <- Stream
                       .fromIterable(list)
                       .mapM(item => if (item.block()) IO.fail(s"Failed for $item.id") else UIO.succeed(item))
                       .buffer(bufferDepth)
                       .process
                       .use(nPulls(_, procDepth))
        filtered = channelOut.collect { case Right(value) => value }
      } yield filtered
  }

  implicit val flwWork = new Worker[Follower] {
    def procEntity(list: List[Follower]): ZIO[zio.console.Console, Nothing, List[Chunk[Follower]]] =
      for {
        channelOut <- Stream
                       .fromIterable(list)
                       .mapM(item => if (item.block()) IO.fail(s"Failed for $item.id") else UIO.succeed(item))
                       .buffer(bufferDepth)
                       .process
                       .use(nPulls(_, procDepth))
        filtered = channelOut.collect { case Right(value) => value }
      } yield filtered
  }
}

object Helper {

  val N           = 10
  val M           = 16
  val bufferDepth = 2
  val procDepth   = 8

  val channels  = (1 to N).map(Channel).toList
  val followers = (1 to M).map(Follower).toList

  def nPulls[R, E, A](pull: ZStream.Pull[R, E, A], n: Int) =
    ZIO.foreach(1 to n)(_ => pull.either)

  // Generic Stream Processor
  def doWork[A: Worker](entries: List[A]) = implicitly[Worker[A]].procEntity(entries)

}

object Timed {
// Queue ops
  def waitForValue[T](ref: UIO[T], value: T): URIO[Clock, T] =
    (ref <* ZIO.sleep(Duration(1, TimeUnit.SECONDS))).repeat(Schedule.doWhile(_ != value))

  def waitForSize[A](queue: Queue[A], size: Int): URIO[Clock, Int] =
    waitForValue(queue.size, size)

  def longSuccess[A](data: A): RIO[Clock, A] = ZIO.sleep(Duration(5, TimeUnit.SECONDS)) *> ZIO.succeed(data)

  val longFail: ZIO[Clock, String, Nothing] = ZIO.sleep(Duration(5, TimeUnit.SECONDS)) *> ZIO.fail("boom")

  val longExcept: RIO[Clock, Nothing] =

    ZIO.sleep(Duration(5, TimeUnit.SECONDS)) *> ZIO.fail(new RuntimeException("Died"))
}
