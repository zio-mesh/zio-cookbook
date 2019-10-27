package hello

import zio.App
import zio.console.{ putStrLn }

object Main extends App {

  def run(args: List[String]) =
    myAppLogic.fold(_ => 1, _ => 0)

  val myAppLogic =
    for {
      _ <- putStrLn("Hello World")
    } yield ()
}
