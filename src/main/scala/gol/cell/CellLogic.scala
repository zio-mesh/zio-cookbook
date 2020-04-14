package gol.cell

import gol.cell.Cell.State
import gol.field._

import zio._
import zio.random.Random

trait CellLogic {
  val cells: CellLogic.Service[Any]
}

object CellLogic {

  trait Service[R] {
    def nextState(c: Cell, f: Field): ZIO[R with FieldLogic with Random, Throwable, Cell]
  }

  class CommonRules(rules: (Cell, Seq[Cell]) => State) extends CellLogic {
    override val cells: Service[Any] = new Service[Any] {
      private def next(c: Cell, neighbours: Seq[Cell]) = ZIO.effect(rules(c, neighbours))

      override def nextState(c: Cell, f: Field): ZIO[FieldLogic with Random, Throwable, Cell] =
        for {
          neighbours <- cellNeighbours(f, c)
          newState   <- next(c, neighbours)
        } yield Cell(c.row, c.col, newState)
    }
  }

  object rules {
    type CellRules = (Cell, Seq[Cell]) => State

    private def buildRulesC(rules: CellRules): CellLogic = new CommonRules(rules)

    private def buildRules(rules: (Cell.State, Seq[Cell]) => State): CellLogic =
      buildRulesC((c, cs) => rules(c.state, cs))

    val allDies: CellLogic = buildRules((_, _) => State.Dead)
    val testingRules: CellLogic = buildRules((cell, neighbours) =>
      cell match {
        case State.Live => if (neighbours.size >= 2) Cell.State.Live else Cell.State.Dead
        case State.Dead => State.Dead
        case _          => State.Dead
      }
    )

    val classicRules: CellLogic = buildRules { (cell, neighbours) =>
      val liveNeighbours = neighbours.count(_.state == State.Live)
      cell match {
        // Any live cell with two or three neighbors survives.
        case State.Live if liveNeighbours == 2 || liveNeighbours == 3 => State.Live
        // Any dead cell with three live neighbors becomes a live cell.
        case State.Dead | State.Empty if liveNeighbours == 3 => State.Live
        // All other live cells die in the next generation. Similarly, all other dead cells stay dead.
        case _ => State.Dead
      }
    }
  }

}
