package zio.cookbook.bracket

import java.io.{ File, FileInputStream }

import zio._
import zio.test.Assertion.{ equalTo, succeeds }
import zio.test._

object BrackSpec extends DefaultRunnableSpec {
  def spec = suite("Bracket Spec")(
    testM("String IO bracket") {
      for {
        str <- IO.succeed("Hello").bracket(_ => IO.succeed("Fail"))(_ => IO.succeed("Pass")).run
        _   = println(str)
      } yield assert(str)(succeeds(equalTo("Pass")))

    },
    testM("File access bracket") {
      for {
        file <- Task(new File("/tmp/hello"))
        // len  = file.length
        string <- Task(new FileInputStream(file))
                   .bracket(BracketLib.closeStream)(BracketLib.convertBytes(_))
      } yield assert(string)(equalTo("one"))
    }
  )
}
