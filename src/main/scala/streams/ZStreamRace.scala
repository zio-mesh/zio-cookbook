package example

import zio.{ Queue, RIO, Ref, ZIO }
import zio.stream.{ Stream }
import zio.duration._
import zio.console.{ putStrLn, Console }

object Tst1 extends zio.App {

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    def process(q1: Queue[Int])(elem: Int) =
      for {
        _  <- q1.offer(elem * 100).when(elem <= 2)
        rv <- zio.random.nextInt(10)
        _  <- ZIO.sleep((rv + 5).millis)
        _  <- putStrLn(s"Processed $elem sleep ${rv + 50} cur: ${System.currentTimeMillis()}")
      } yield elem

    val valsLst     = (1 to 2).toList
    val inputStream = Stream.fromIterable(valsLst).tap(e => putStrLn(s"IN $e"))

    def readTask(queue: Queue[Int], ref: Ref[Int]): ZIO[Console, Nothing, Unit] =
      inputStream
        .merge(Stream.fromQueue(queue))
        .buffer(4)
        .tap(vv => putStrLn(s"Read $vv"))
        .grouped(2)
        .tap(batch => queue.offerAll(batch))
        .tap(batch => ref.update(_ + batch.size))
        .runDrain

    def procTask(queue: Queue[Int], refProc: Ref[Int], refLoad: Ref[Int]) =
      Stream
        .fromQueue(queue)
        .tap(process(queue))
        .tap(_ => refProc.update(_ + 1))
        .tap(_el =>
          for {
            _  <- putStrLn("zel:" + _el)
            q  <- queue.size
            lc <- refLoad.get
            pc <- refProc.get
            _ <- putStrLn(
                  "el:" + _el.toString + " ps: " + (q, lc, pc).toString + s" stop: ${q <= 0 && lc >= 0 && lc == pc}"
                )
            finish = q <= 0 && lc >= 0 && lc == pc
          } yield finish
        )
        .runDrain

    val logic: RIO[zio.ZEnv, Unit] = for {
      _         <- putStrLn("Start")
      q1        <- Queue.bounded[Int](10)
      loaded    <- Ref.make(0)
      processed <- Ref.make(0)

      readFiber    <- readTask(q1, loaded).fork
      processFiber <- procTask(q1, processed, loaded).fork

      _ <- putStrLn("Pre Stop")
      _ <- (readFiber *> processFiber).join
      _ <- q1.shutdown
      _ <- putStrLn("Stop")
    } yield ()

    logic.fold(_ => 1, _ => 0)
  }
}
