package tagless

import zio.{ Task, ZIO }
import simulacrum.{ typeclass }

@typeclass
trait Eff[F[_]] {
  def hello[A](s: String): F[Unit]
}

object Eff {
  implicit val taskEff = new Eff[Task] {
    def hello[A](s: String): Task[Unit] = Task.succeed(println(s))
  }

  // implicit val fibEff = new Eff[Fiber[Nothing,?]] {

  // }

  // implicit val funcEff = new Eff[FunctionIO[Unit] {
  //   def hello(s: String): FunctionIO[Unit] = FunctionIO.succeed(println(s))
  // }

}

import Eff._

object App0 extends App {
  val eff0 = Eff[Task].hello("Task")
  // val eff1 = Eff[FunctionIO].hello("FunctionIO")

  eff0.foldM(_ => ZIO.dieMessage("Fatal"), _ => ZIO.succeed(0))
}
