package ladder

import zio._
import zio.duration._
import zio.internal.PlatformLive

object Ladder extends App {
  override val platform = PlatformLive.Default.withReportFailure(_ => ())

  override def run(args: List[String]) = {
    val rubles = List.fill(50)(2)
    for {
      work <- Queue.unbounded[Int]
        .tap(_.offerAll(rubles))
      output <- Queue.unbounded[Int]
      workers <- ZIO.traverse(1 to 5) {
        i =>
          val job = if (i == 3) {
            (_: Int) => ZIO.fail("third guy dies")
          } else {
            output.offer _
          }

          work.take.flatMap(i => job(i).onError(_ => work.offer(i))).forever.fork
      }
      _ <- {
        def go(count: Int): URIO[ZEnv, Unit] = {
          if (count == 100) {
            console.putStrLn(s"Got all rubles: $count")
          } else {
            output.take.flatMap { i =>
              console.putStrLn(s"Got ruble: $i+$count") *>
              go(i + count)
            }
          }
        }

        go(0).timeoutFail("Not enough rubles")(5.seconds) *>
          work.shutdown *>
          Fiber.awaitAll(workers)
      }
    } yield ()
  }.foldM(ZIO.dieMessage _, _ => ZIO.succeed(0))
}