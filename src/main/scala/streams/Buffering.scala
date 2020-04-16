package streams

import Timed._

import zio.console.putStrLn
import zio.stream.{ Stream, ZStream }
import zio.{ App, IO, Queue }

object App3 extends App {
  def run(args: List[String]) =
    app.as(0)

  val app = for {
    queue <- Queue.bounded[Int](10)
    _     <- ZStream(1, 2, 3, 4).mapMPar(2)(queue.offer).runCollect
    out   <- queue.takeAll
    _     <- putStrLn(out.toString)
  } yield ()
}

object App4 extends App {
  def run(args: List[String]) =
    app.as(0)

  val list = List(1, 2, 3)

  val app = for {
    queue <- Queue.bounded[Int](10)
    _     <- queue.offerAll(list)
    fiber <- Stream
              .fromQueue(queue)
              .foldWhileM(List[Int]())(_ => true)((acc, el) => IO.succeed(el :: acc))
              .map(_.reverse)
              .fork
    _     <- waitForSize(queue, -1)
    _     <- queue.shutdown
    items <- fiber.join
    _     <- putStrLn(items.toString)
  } yield ()
}
