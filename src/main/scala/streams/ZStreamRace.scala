package streams

import schedule.Common.getSecondsTimestamp

import zio.console.{ putStrLn, Console }
import zio.stream.{ Stream }
import zio.{ App, Promise, Queue, Ref, ZIO }

object App7 extends App {

  override def run(args: List[String]) = logic.exitCode

  def process(data: List[Int]) = ZIO.succeed(data.map(_ * 10))

  val inputData   = List(1, 2, 3, 4)
  val inputStream = Stream.fromIterable(inputData)

  def readTask(queue: Queue[Int], ref: Ref[List[Int]]): ZIO[Any, Nothing, Unit] =
    for {
      elems <- ZIO.succeed(inputData)
      _     <- queue.offerAll(elems)
      _     <- ref.update(_ ++ elems)
    } yield ()

  def procTask(queue: Queue[Int], ref: Ref[List[Int]]): ZIO[Console, Nothing, Unit] =
    for {
      _    <- putStrLn(s"Processing at time $getSecondsTimestamp")
      din  <- queue.takeAll
      dout <- process(din)
      _    <- ref.update(_ ++ dout)
    } yield ()

  def procEff(queue: Queue[Int], ref: Ref[List[Int]]): ZIO[Any, Nothing, Unit] =
    procTask(queue, ref).provideLayer(Console.live)

  val logic = for {
    _         <- putStrLn("Start")
    queue     <- Queue.bounded[Int](10)
    loaded    <- Ref.make(List.empty[Int])
    processed <- Ref.make(List.empty[Int])
    latch     <- Promise.make[Nothing, Unit]

    loadFiber    <- inputStream.merge(Stream.fromEffect(readTask(queue, loaded))).runDrain.fork
    processFiber <- Stream.fromEffect(procEff(queue, processed)).ensuring(latch.succeed(())).runCollect.fork

    _    <- (loadFiber *> processFiber).join
    _    <- latch.await
    din  <- loaded.get
    dout <- processed.get

    _ <- putStrLn(s"loaded list: ${din}")
    _ <- putStrLn(s"processed list: ${dout}")
    _ <- queue.shutdown
  } yield ()
}
