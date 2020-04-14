package tagless

import scala.concurrent.Future

import zio.console.{ putStrLn }
import zio.{ Runtime, Task }

trait Greet[F[_]] {
  def sayHello(): F[String]
}

object Greet {
  type Id[A] = A

  implicit val idGreet: Greet[Id] = new Greet[Id] {
    def sayHello(): Id[String] = "Hello Id"
  }

  implicit val futGreet: Greet[Future] = new Greet[Future] {
    def sayHello(): Future[String] = Future.successful("Hello Future")
  }

  implicit val zioGreet: Greet[Task] = new Greet[Task] {
    def sayHello(): Task[String] = Task.succeed("Hello ZIO")
  }
}
import Greet._

object Main extends App {
  val greet0 = idGreet
  val greet1 = futGreet
  val greet2 = zioGreet

  val rt = Runtime.default
  println(greet0.sayHello)
  println(greet1.sayHello)
  rt.unsafeRun(greet2.sayHello >>= (v => putStrLn(v.toString)))
}
