package streams

import zio.console.putStrLn
import zio.stream._
import zio.{ App }

object App0 extends App {
  def run(args: List[String]) = app0.exitCode

  def streamReduce(total: Int, element: Int): Int = total + element

  val app0 = for {
    sum    <- Stream(1, 2, 3).run(Sink.foldLeft(0)(streamReduce))
    _      <- putStrLn(sum.toString)
    merged <- Stream(1, 2, 3).merge(Stream(2, 3, 4)).runCollect.run
    _      <- putStrLn(merged.toString)

  } yield ()

}
