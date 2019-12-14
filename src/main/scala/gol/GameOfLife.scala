package gol
import gol.cell.Cell.State
import gol.cell._
import gol.field._
import gol.gui.GUI.WindowController
import gol.gui._
import zio._
import zio.clock._
import zio.console._
import zio.duration._
import zio.random.Random

import scala.util.Try

object GameOfLife extends App {

  type GOLTestEnv = FieldLogic with CellLogic with Random with Console with Clock
  private def buildEnv(maxW: Int, maxH: Int, cellLogic: CellLogic): GOLTestEnv =
    new FieldLogic with CellLogic with Random.Live with Console.Live with Clock.Live {
      override val field: FieldLogic.Service[Any] = new FieldLogic.StdRules(maxW, maxH, true).field
      override val cells: CellLogic.Service[Any]  = cellLogic.cells
    }

  private def clearConsole = ZIO.effect {
    System.out.print("\033[H\033[2J")
    System.out.flush()
  }

  private def renderField(turn: Long, f: Field) = {
    def stateToSymbol(st: Cell.State) = st match {
      case State.Live  => "1"
      case State.Dead  => "0"
      case State.Empty => " "
    }

    clearConsole *> ZIO.effectTotal {
      val rendered = f.cells.map {
        _.map { cell =>
          stateToSymbol(cell.state)
        }.mkString("")
      }.mkString("\n")
      println(s"TURN: $turn\n$rendered\n")
    }
  }

  private def persistField(f: Field) = ZIO.effect {
    f.mapRows(_.state match {
        case State.Live => "1"
        case _          => "0"
      })
      .map(_.mkString(" "))
      .mkString("\n")
  }
  private def parseField(str: String) = {
    val columnsToParse = str.indexOf('\n') / 2 + 1
    println(columnsToParse)
    val statesM = ZIO.effect(
      str
        .split(Array(' ', '\n'))
        .toList
        .map {
          case "1" => State.Live
          case _   => State.Dead
        }
        .grouped(columnsToParse)
        .toList
    )
    for {
      parsedStates <- statesM
      _            <- ZIO.effect(println(parsedStates))
      loaded <- fieldT(
                 (x, y) =>
                   ZIO.effect {
                     try {
                       parsedStates(x)(y)
                     } catch {
                       case _: Throwable => Cell.State.Dead
                     }
                   }
               )
    } yield loaded
  }

  val Blinker: String =
    "0 0 0 0 0\n" +
      "0 0 1 0 0\n" +
      "0 0 1 0 0\n" +
      "0 0 1 0 0\n" +
      "0 0 0 0 0\n"

  val Toad: String =
    """0 0 0 0 0 0
      |0 0 0 0 0 0
      |0 0 1 1 1 0
      |0 1 1 1 0 0
      |0 0 0 0 0 0
      |0 0 0 0 0 0
      |""".stripMargin

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val cols = 30
    val rows = 30
    val env  = buildEnv(cols, rows, CellLogic.rules.classicRules)

    val uiLogic = {
      def updateUi(queue: Queue[Field], ctrl: WindowController) =
        for {
          upd <- queue.take
          _   <- ctrl.update(upd)
          _   <- sleep(500.millis)
        } yield ()

      def buildUi(queue: Queue[Field]) =
        for {
          ctrl <- GUI.window(rows, cols)
          _    <- updateUi(queue, ctrl).forever.fork
        } yield ()

      def runGameLogic(cur: Field, queue: Queue[Field]): ZIO[GOLTestEnv, Throwable, Unit] =
        for {
          newField     <- field.makeTurn(cur)
          _            <- queue.offer(newField)
          continueGame = newField.allCells.exists(_.state == State.Live)
          _            <- runGameLogic(newField, queue).when(continueGame)
          _            <- renderField(cur.turn, cur).when(!continueGame)
        } yield ()

      for {
        queue        <- Queue.bounded[Field](100)
        initialFiled <- field.randomField
        _            <- queue.offer(initialFiled)
        _            <- buildUi(queue)
        fiber        <- runGameLogic(initialFiled, queue).fork
        _            <- fiber.join
        fiber2       <- ZIO.unit.forever.fork
        _            <- queue.awaitShutdown
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
