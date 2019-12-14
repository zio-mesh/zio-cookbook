package gol
import gol.cell.Cell.State
import gol.cell._
import gol.field._
import zio._
import zio.clock._
import zio.duration._
import zio.console.Console
import zio.random.Random

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
      case State.Live  => "+"
      case State.Dead  => " "
      case State.Empty => " "
    }

    clearConsole *>
      ZIO.effectTotal {
        val rendered = f.cells.map {
          case (_, col) => col.map { case (_, cell) => stateToSymbol(cell.state) }.mkString("")
        }.mkString("\n")
        println(s"""TURN: $turn
                   |$rendered
                   |""".stripMargin)
      } *> sleep(1.second * (1f / 3))
  }

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val env     = buildEnv(20, 20, CellLogic.rules.classicRules2)
    val maxTurn = 100

    def runGame(f: Field, turn: Long): ZIO[GOLTestEnv, Throwable, Unit] =
      for {
        _        <- ZIO.effect(println(turn))
        newField <- field.makeTurn(f)
        _        <- renderField(turn, newField)
        _        <- runGame(newField, turn + 1).when(f != newField && turn < maxTurn)
      } yield ()

    val logic = for {
      rndField <- field.randomField
      _        <- runGame(rndField, 1)
    } yield ()

    logic
      .provide(env)
      .fold(ex => {
        println(ex.getMessage)
        ex.printStackTrace()
        1
      }, _ => 0)
  }
}
