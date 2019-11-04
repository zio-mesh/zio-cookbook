package zio.cookbook.bracket

import java.io.{ File, FileInputStream }

import zio.{ IO, Task }
import zio.test.{ assert, assertM, suite, testM, DefaultRunnableSpec }
import zio.test.Assertion.{ equalTo, succeeds }

object BrackSpec
    extends DefaultRunnableSpec(
      suite("Bracket Spec")(
        testM("String IO bracket") {
          for {
            str <- IO.succeed("Hello").bracket(_ => IO.succeed("Fail"))(_ => IO.succeed("Pass")).run
            _   = println(str)
          } yield assert(str, succeeds(equalTo("Pass")))

        },
        testM("File access bracket") {
          val mybracket = for {
            file   <- Task(new File("/tmp/hello"))
            len    = file.length
            string <- Task(new FileInputStream(file)).bracket(BracketLib.closeStream)(BracketLib.convertBytes(_, len))
          } yield string

          assertM(mybracket.run, equalTo(List("one", "two")))

        }
      )
    )
