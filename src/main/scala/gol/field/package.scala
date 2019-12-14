package gol
import gol.cell.{ Cell, CellLogic }
import zio.{ Task, ZIO }
import zio.random.Random

package object field extends FieldLogic.Service[FieldLogic with Random] {
  override def randomField: FieldTask[Field] = ZIO.accessM(_.field.randomField)
  override def cellNeighbours(field: Field, x: Int, y: Int): FieldTask[Seq[Cell]] =
    ZIO.accessM(_.field.cellNeighbours(field, x, y))

  override def constantFiled(fieldState: Task[Cell.State]): FieldTask[Field] =
    ZIO.accessM(_.field.constantFiled(fieldState))

  override def makeTurn(f: Field): FieldTaskR[CellLogic with FieldLogic with Random, Field] =
    ZIO.accessM(_.field.makeTurn(f))
}
