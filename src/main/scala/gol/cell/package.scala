package gol

import gol.field.{ Field, FieldLogic }

import zio.ZIO
import zio.random.Random

package object cell extends CellLogic.Service[CellLogic] {
  override def nextState(c: Cell, f: Field): ZIO[CellLogic with FieldLogic with Random, Throwable, Cell] =
    ZIO.accessM(_.cells.nextState(c, f))
}
