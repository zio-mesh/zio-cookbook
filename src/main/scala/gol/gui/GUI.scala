package gol.gui

import gol.cell.Cell.State
import gol.field.Field
import java.awt._
import javax.swing._
import zio._

object GUI {
  def window(rows: Int, cols: Int): Task[WindowController] = ZIO.effect {
    val frame    = new JFrame("Game Of Life")
    val grid     = new GridLayout(rows, cols, 5, 5)
    val mainPane = new JPanel(grid, true)
    val cells = (0 until (rows * cols)).map { _ =>
      val cellPane = new JPanel()
      cellPane.setBackground(Color.GRAY)
      cellPane.setSize(new Dimension(10, 10))
      mainPane.add(cellPane)
      cellPane
    }
    frame.setContentPane(mainPane)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.setSize(new Dimension(600, 600))
    frame.setLocationRelativeTo(null)
    frame.pack()
    frame.setVisible(true)
    WindowController(frame, cells)
  }

  case class WindowController(frame: JFrame, cells: Seq[JPanel]) {
    def update(f: Field): Task[Unit] = ZIO.effect {
      frame.setTitle(s"Game Of Life. Turn ${f.turn}")
      (f.allCells zip cells) map {
        case (cell, panel) =>
          val color = cell.state match {
            case State.Live => Color.BLACK
            case State.Dead => Color.WHITE
            case _          => Color.WHITE
          }
          panel.setBackground(color)
      }
    }
  }
}
