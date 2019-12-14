package gol.field

import gol.cell._
import zio._
import zio.random._

import scala.collection.immutable

trait FieldLogic {
//  val field: FieldLogic.Service[Any with Random]
  val field: FieldLogic.Service[Any]
}

object FieldLogic {
  trait Service[R] {
    type FieldTaskR[RR, T] = RIO[R with RR, T]
    type FieldTask[T]      = FieldTaskR[R with Random, T]

    def constantFiled(fieldState: Task[Cell.State]): FieldTask[Field]
    def constantFiled(fieldState: Cell.State): FieldTask[Field] = constantFiled(ZIO.effectTotal(fieldState))

    def randomField: FieldTaskR[Random, Field]

    def cellNeighbours(field: Field, x: Int, y: Int): FieldTask[Seq[Cell]]
    def cellNeighbours(field: Field, f: Cell): FieldTask[Seq[Cell]] = cellNeighbours(field, f.x, f.y)

    def makeTurn(f: Field): FieldTaskR[CellLogic with FieldLogic with Random, Field]
  }

  class StdRules(w: Int, h: Int, allowDiagonals: Boolean = false) extends FieldLogic {
    override val field: Service[Any] = new Service[Any] {

      def constantFiled(fieldState: Task[Cell.State]): FieldTask[Field] = fieldState.map { state =>
        val cells = for {
          x <- 0 until w
          y <- 0 until h
        } yield Cell(x, y, state)
        Field(w, h, regroup(cells))
      }

      override def randomField: FieldTaskR[Random, Field] = {
        val coords = ZIO.effectTotal(for {
          x <- 0 until w
          y <- 0 until h
        } yield (x, y))

        def randomCellState =
          nextInt(3).map(idx => Cell.State.formIdx(idx))

        def randomCell(x: Int, y: Int) = randomCellState.map(s => Cell(x, y, s))

        for {
          coords     <- coords
          cellStates <- ZIO.traversePar(coords) { case (x, y) => randomCell(x, y) }
        } yield {
          val rows = cellStates.groupBy(_.x)
          val fMap = rows.mapValues(_.map(v => v.y -> v).toMap)
          Field(w, h, fMap)
        }
      }

      private def neighboursCoords(x: Int, y: Int) =
        if (allowDiagonals)
          (for {
            xs <- x - 1 to x + 1
            ys <- y - 1 to y + 1
          } yield (xs, ys)).filterNot { case (xx, yy) => xx == x & yy == y } else
          Seq((x - 1) -> y, (x + 1) -> y, x -> (y - 1), x -> (y + 1))

      override def cellNeighbours(field: Field, x: Int, y: Int): FieldTask[Seq[Cell]] = ZIO.effect {
        neighboursCoords(x, y) map {
          case (xs, ys) =>
            if ((xs >= 0 && ys >= 0) && (xs < w && ys < h)) field.cells(xs)(ys)
            else Cell(xs, ys, Cell.State.Empty)
        }
      }

      override def makeTurn(f: Field): FieldTaskR[CellLogic with FieldLogic with Random, Field] =
        for {
          newCells <- ZIO.foreach(f.allCells)(nextState(_, f))
        } yield {
          Field(f.w, f.h, regroup(newCells))
        }
    }
  }

  private def regroup(cells: Seq[Cell]) =
    cells.groupBy(_.x).mapValues(_.map(v => v.y -> v).toMap)
}
