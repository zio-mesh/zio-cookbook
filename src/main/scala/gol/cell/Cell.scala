package gol.cell

import gol.cell.Cell.State

final case class Cell(row: Int, col: Int, state: State)
object Cell {
  sealed trait State extends Product with Serializable
  object State {
    @inline def formIdx(idx: Int): State = idx match {
      case 0 => Live
      case 1 => Dead
      case 2 => Empty
    }

    case object Empty extends State
    case object Live  extends State
    case object Dead  extends State
  }
}
