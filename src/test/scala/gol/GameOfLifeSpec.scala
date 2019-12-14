package gol

import gol.Helpers._
import gol.cell.Cell.State
import gol.cell.CellLogic._
import gol.cell.{ Cell, CellLogic }
import gol.field.{ Field, FieldLogic }
import zio.random.Random
import zio.test.Assertion.equalTo
import zio.test.{ assert, suite, testM, DefaultRunnableSpec }
import zio._
import zio.console._

object Helpers {

  val maxW = 5
  val maxH = 5

  type GOLTestEnv = FieldLogic with CellLogic with Random with Console
  private def buildTestEnv(cellLogic: CellLogic): Task[GOLTestEnv] = ZIO.effectTotal(
    new FieldLogic with CellLogic with Random.Live with Console.Live {
      override val field: FieldLogic.Service[Any] = new FieldLogic.StdRules(maxW, maxH).field
      override val cells: CellLogic.Service[Any]  = cellLogic.cells
    }
  )

  def testEnv(cellLogic: CellLogic): UManaged[GOLTestEnv] = Managed.fromEffect(buildTestEnv(cellLogic)).orDie

  def renderField(f: Field): UIO[String] = ZIO.effectTotal {
    f.cells.map {
      case (x, col) => col.map { case (y, cell) => s"[$x, $y] ${cell.state}" }.mkString("'", "','", "'")
    }.mkString("\n")
  }
}
object GameOfLifeSpec
    extends DefaultRunnableSpec(
      suite("GameOfLife")(
        suite("uses game rules")(
          suite("allDies")(
            testM("dead for single cell")(
              for {
                allDeadCells <- field.constantFiled(Cell.State.Dead)
                fieldCell    = allDeadCells.cells(maxH / 2)(maxW / 2)
                rr           <- cell.nextState(fieldCell, allDeadCells)
              } yield assert(rr.state, equalTo(Cell.State.Dead))
            ),
            testM("returns all dead")(
              for {
                allDeadCells <- field.constantFiled(Cell.State.Dead)
                rr           <- field.makeTurn(allDeadCells)
              } yield assert(rr, equalTo(allDeadCells))
            )
          ).provideManaged(testEnv(rules.allDies))
        )
      )
    )
