package streams

import zio.console.putStrLn
import zio.stream.ZStream
import zio.{ App, Queue }

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
