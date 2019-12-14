package gol.field

import gol.cell._
import zio._
import zio.random._

trait FieldLogic {
  val field: FieldLogic.Service[Any]
}

object FieldLogic {
  trait Service[R] {
    type FieldTaskR[RR, T] = RIO[R with RR, T]
    type FieldTask[T]      = FieldTaskR[R with Random, T]

    def fieldT(fieldState: (Int, Int) => Task[Cell.State]): FieldTask[Field]
    def fieldT(fieldState: Task[Cell.State]): FieldTask[Field]  = fieldT((_, _) => fieldState)
    def constantField(fieldState: Cell.State): FieldTask[Field] = fieldT(ZIO.effectTotal(fieldState))
    def allDeadField: FieldTask[Field]                          = constantField(Cell.State.Dead)

    def randomField: FieldTaskR[Random, Field]

    def cellNeighbours(field: Field, x: Int, y: Int): FieldTask[Seq[Cell]]
    def cellNeighbours(field: Field, f: Cell): FieldTask[Seq[Cell]] = cellNeighbours(field, f.row, f.col)

    def makeTurn(f: Field): FieldTaskR[CellLogic with FieldLogic with Random, Field]
  }

  class StdRules(rows: Int, cols: Int, allowDiagonals: Boolean = false) extends FieldLogic {
    override val field: Service[Any] = new Service[Any] {

      override def fieldT(fieldState: (Int, Int) => Task[Cell.State]): FieldTask[Field] = {
        val cells = ZIO.collectAll((0 until rows) map { row =>
          ZIO.collectAll((0 until cols) map (col => fieldState(row, col).map(state => Cell(row, col, state))))
        })
        cells.map(cs => Field(rows, cols, cs))
      }

      override def randomField: FieldTaskR[Random, Field] = {
        val randomCellState = nextInt(3).map(idx => Cell.State.formIdx(idx))
        for {
          rnd <- ZIO.environment[Random]
          fld <- fieldT(randomCellState.provide(rnd))
        } yield fld
      }

      private def neighboursCoords(row: Int, col: Int) =
        if (allowDiagonals)
          (for {
            r <- row - 1 to row + 1
            c <- col - 1 to col + 1
          } yield (r, c)).filterNot { case (xx, yy) => xx == row & yy == col } else
          Seq((row - 1) -> col, (row + 1) -> col, row -> (col - 1), row -> (col + 1))

      override def cellNeighbours(field: Field, row: Int, col: Int): FieldTask[Seq[Cell]] = ZIO.effect {
        neighboursCoords(row, col) map {
          case (r, c) =>
            if ((r >= 0 && c >= 0) && (r < rows && c < cols)) field.cells(r)(c)
            else Cell(r, c, Cell.State.Dead)
        }
      }

      override def makeTurn(f: Field): FieldTaskR[CellLogic with FieldLogic with Random, Field] = {
        def mapRow(row: List[Cell]) = ZIO.collectAll(row.map(cell => nextState(cell, f)))

        ZIO
          .foreach(f.cells)(mapRow)
          .map(newCells => Field(f.rows, f.cols, newCells, f.turn + 1))
      }
    }
  }
}
