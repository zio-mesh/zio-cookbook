package gol

import gol.cell._
import gol.field._
import gol.gui.GUI.GameField.Constraints
import gol.gui._
import zio._
import zio.clock._
import zio.console._
import zio.duration._
import zio.random._

object GameOfLife extends App {

  type GOLEnv = FieldLogic with CellLogic with Random with Console with Clock
  private def buildEnv(maxW: Int, maxH: Int, cellLogic: CellLogic): GOLEnv =
    new FieldLogic with CellLogic with Random.Live with Console.Live with Clock.Live {
      override val field: FieldLogic.Service[Any] = FieldLogic.StdRules(maxW, maxH, allowDiagonals = true).field
      override val cells: CellLogic.Service[Any]  = cellLogic.cells
    }

//  val Blinker: String =
//    "0 0 0 0 0\n" +
//      "0 0 1 0 0\n" +
//      "0 0 1 0 0\n" +
//      "0 0 1 0 0\n" +
//      "0 0 0 0 0\n"
//
//  val Toad: String =
//    """0 0 0 0 0 0
//      |0 0 0 0 0 0
//      |0 0 1 1 1 0
//      |0 1 1 1 0 0
//      |0 0 0 0 0 0
//      |0 0 0 0 0 0
//      |""".stripMargin

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val cols = 150
    val rows = 100
    val env  = buildEnv(cols, rows, CellLogic.rules.classicRules)

    val uiLogic = {

      def buildUi(queue: Queue[Field], speed: Ref[Int]) =
        GUI.GameField.build(cols, rows, queue, speed, Constraints(10, 10, 2)).unit

      def runGameLogic(cur: Field, queue: Queue[Field], frameDelay: Ref[Int]): ZIO[GOLEnv, Throwable, Unit] =
        for {
          newField <- field.makeTurn(cur)
          _        <- queue.offer(newField)
          delay    <- frameDelay.get
          _        <- sleep(delay.millis)
          _        <- runGameLogic(newField, queue, frameDelay)
        } yield ()

      for {
        frameDelay   <- Ref.make(0)
        fieldUpdates <- Queue.bounded[Field](10)
        _            <- buildUi(fieldUpdates, frameDelay).fork
        initialFiled <- field.randomField
        _            <- fieldUpdates.offer(initialFiled)
        fiber        <- runGameLogic(initialFiled, fieldUpdates, frameDelay).fork
        _            <- fiber.join
        _            <- fieldUpdates.awaitShutdown
        fiber2       <- ZIO.unit.forever.fork
        _            <- fiber2.join
      } yield ()
    }

    uiLogic
      .provide(env)
      .fold(ex => {
        println(ex.getMessage)
        ex.printStackTrace()
        1
      }, _ => 0)
  }
}
