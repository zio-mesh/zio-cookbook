package gol.gui

import java.awt._
import java.awt.image.{ BufferStrategy, BufferedImage }

import gol.cell.Cell
import gol.cell.Cell.State
import gol.field.Field
import javax.swing._
import sun.awt.image.OffScreenImage
import zio._

object GUI {

  object colors {
    val live: Color       = Color.GREEN.darker()
    val dead: Color       = Color.LIGHT_GRAY
    val background: Color = Color.GRAY

    @inline
    def stateToColor(state: Cell.State): Color = state match {
      case State.Live => colors.live
      case _          => colors.dead
    }
  }

  object GameField {

    case class Constraints(cellH: Int, cellW: Int, gap: Int) {
      def maxH(cols: Int): Int = (cellW + gap) * cols + gap

      def maxW(rows: Int): Int = (cellH + gap) * rows + gap

      def totalW(field: Field): Int = (cellW + gap) * field.cols

      def totalH(field: Field): Int = (cellH + gap) * field.cols

      def coords(cell: Cell): (Int, Int) = {
        val x = (cellW + gap) * cell.row + gap
        val y = (cellH + gap) * cell.col + gap
        (x, y)
      }
    }

    private def draw(field: Field, g: Graphics, realW: Int, realH: Int) = {
      def doDraw(g: Graphics, constraints: Constraints): Unit = {
        g.setColor(colors.background)
        g.fillRect(0, 0, realW, realH)

        field.allCells.foreach { cell =>
          val (x, y) = constraints.coords(cell)
          val color  = colors.stateToColor(cell.state)
          g.setColor(color)
          g.fillRect(x, y, constraints.cellW, constraints.cellH)
        }
      }

      for {
        constraints <- ZIO.environment[Constraints]
        _           <- ZIO.effect(doDraw(g, constraints))
      } yield ()
    }

    implicit class GraphicsOps(private val g: Graphics) extends AnyVal {
      def drawImageM(img: Image, x: Int, y: Int): Task[Unit] = ZIO.effect(g.drawImage(img, x, y, null)).unit
    }

    implicit class BufferStrategyOps(private val bs: BufferStrategy) extends AnyVal {
      def graphicsM: TaskManaged[Graphics] = Managed.makeEffect(bs.getDrawGraphics) { gr =>
        gr.dispose()
        bs.show()
      }
    }

    private val startTime: Long = System.currentTimeMillis()

    def build(
      cols: Int,
      rows: Int,
      updates: Queue[Field],
      frameDelay: Ref[Int],
      constraints: Constraints
    ): Task[Unit] = {
      val maxW = constraints.maxW(cols)
      val maxH = constraints.maxH(cols)

      val frameAndBuffer = {
        val dimension = new Dimension(640, 480)
        val frame = ZIO.effect {
          val frame = new JFrame("Game Of Life")
          frame.setSize(dimension)
          frame.setBackground(colors.background)
          frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
          frame.setLocationRelativeTo(null)
          frame.setLayout(new BorderLayout())
          frame
        }

        val scrollPane = ZIO.effect {
          val scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS)
          scrollPane.setBackground(colors.background)
          scrollPane.setPreferredSize(dimension)
          scrollPane
        }

        val canvas = ZIO.effect {
          val canvas = new Canvas()
          canvas.setPreferredSize(new Dimension(maxW, maxH))
          canvas.setIgnoreRepaint(true)
          canvas.setBackground(colors.background)
          canvas
        }

        def trimDelay(d: Int): Int = if (d < 0) 0 else d
        def bindButton(title: String, speed: Ref[Int], mod: Int => Int) =
          for {
            button <- ZIO.effect(new Button(title))
            _ <- IO
                  .effectAsync[Throwable, Unit] { cb =>
                    button.addActionListener { _ =>
                      val logic = for {
                        curSpeed <- speed.get
                        newSpeed <- ZIO.effect(trimDelay(mod(curSpeed)))
                        _        <- speed.set(newSpeed)
                      } yield ()
                      cb(logic)
                    }
                  }
                  .forever
                  .fork
          } yield button

        val controlls = for {
          bpane <- ZIO.effect {
                    val buttonsPane = new JPanel()
                    buttonsPane.setLayout(new BoxLayout(buttonsPane, BoxLayout.X_AXIS))
                    buttonsPane
                  }
          minus10Delay  <- bindButton("-10ms", frameDelay, _ - 10)
          minus100Delay <- bindButton("-100ms", frameDelay, _ - 100)
          plus100Delay  <- bindButton("+100ms", frameDelay, _ + 100)
          plus10Delay   <- bindButton("+10ms", frameDelay, _ + 10)
          _ <- ZIO.effect {
                bpane.add(minus10Delay)
                bpane.add(minus100Delay)
                bpane.add(plus100Delay)
                bpane.add(plus10Delay)
              }
        } yield bpane

        for {
          fr      <- frame
          scroll  <- scrollPane
          can     <- canvas
          buttons <- controlls
          bs <- ZIO.effect {
                 fr.add(buttons, BorderLayout.NORTH)
                 fr.add(scroll, BorderLayout.CENTER)
                 scroll.add(can)
                 fr.pack()
                 fr.setVisible(true)
                 can.createBufferStrategy(2)
                 can.getBufferStrategy
               }
        } yield (fr, can, bs, scroll)

      }

      def processUpdate(
        buff: BufferStrategy,
        imgRef: Ref[BufferedImage],
        frame: JFrame,
        canvas: Canvas,
        scroll: ScrollPane
      ) = {
        val realImage = imgRef.updateSome {
          case image: OffScreenImage
              if image.getWidth != canvas.getSize.getWidth || image.getHeight != canvas.getSize.getHeight =>
            new BufferedImage(
              canvas.getSize.getWidth.toInt,
              canvas.getSize.getHeight.toInt,
              BufferedImage.TYPE_INT_ARGB
            )
          case image: OffScreenImage => image
        }
        for {
          img         <- realImage
          imgGraphics <- ZIO.effectTotal(img.getGraphics)
          field       <- updates.take
          delay       <- frameDelay.get
          canvasW     = canvas.getSize().width
          canvasH     = canvas.getSize.height
          _ <- ZIO.effectTotal {
                val x = scroll.getScrollPosition.x
                val y = scroll.getScrollPosition.y
                imgGraphics.clipRect(x, y, canvasW, canvasH)
              }
          fiber <- draw(field, imgGraphics, canvasW, canvasH).provide(constraints).fork
          _     <- fiber.join
          _     <- buff.graphicsM.use(_.drawImageM(img, 0, 0))

          rps <- ZIO.effectTotal {
                  val cur     = System.currentTimeMillis()
                  val timeSec = (cur - startTime) / 1000.toFloat
                  field.turn.toFloat / timeSec
                }
          _ <- ZIO.effectTotal(frame.setTitle(s"Game Of Life. Turn ${field.turn} : $delay ms  RPS: $rps"))
        } yield ()
      }

      for {
        (frame, canvas, buffStrategy, scroll) <- frameAndBuffer
        img                                   <- Ref.make(new BufferedImage(maxW, maxH, BufferedImage.TYPE_INT_ARGB))
        _                                     <- processUpdate(buffStrategy, img, frame, canvas, scroll).forever
      } yield ()
    }
  }
}
