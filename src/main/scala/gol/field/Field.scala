package gol.field

import gol.cell.Cell

final case class Field(w: Int, h: Int, cells: Map[Int, Map[Int, Cell]]) {
  def render: String =
    (0 until w).map { x =>
      val curRow = cells(x)
      curRow.map(_._2.state).mkString("[", ", ", "]")
    }.mkString("[", ",\n", "]")

  def allCells: Seq[Cell] = cells.values.flatMap(_.values).toSeq

}
