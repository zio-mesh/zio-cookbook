package tagless

import simulacrum.{ typeclass }

import zio.{ Task }

@typeclass
trait Eff[F[_]] {
  def hello[A](s: String): F[Unit]
}

object Eff {
  implicit val taskEff = new Eff[Task] {
    def hello[A](s: String): Task[Unit] = Task.succeed(println(s))
  }
}

import Eff._

object App0 extends App {
  val eff0 = Eff[Task].hello("Task")

  eff0.exitCode
}
