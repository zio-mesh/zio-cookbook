package tagless

import scala.concurrent.Future
import zio.{ DefaultRuntime, Task }
import zio.console.{ putStrLn }

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

  val rt = new DefaultRuntime {}
  println(greet0.sayHello)
  println(greet1.sayHello)
  rt.unsafeRun(greet2.sayHello >>= putStrLn)
}
