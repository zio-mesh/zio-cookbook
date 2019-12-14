package gol.field

import gol.cell.Cell

final case class Field(rows: Int, cols: Int, cells: List[List[Cell]], turn: Long = 0) {
  def mapRows[T](f: Cell => T): List[List[T]] = cells.map(_.map(f))
  def mapStates(f: Cell => Cell.State): Field =
    copy(cells = cells.map(_.map(cell => cell.copy(state = f(cell)))), turn = turn + 1)

  def render: String =
    (0 until rows).map { row =>
      val curRow = cells(row)
      curRow.map(_.state).mkString("[", ", ", "]")
    }.mkString("[", ",\n", "]")

  def allCells: Seq[Cell] = cells.flatten

}
