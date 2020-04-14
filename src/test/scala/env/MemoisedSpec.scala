package env

import cats.Eval

import zio.duration._
import zio.test.Assertion._
import zio.test._
import zio.{ test => _, _ }

object ToTest {
  var callCount: Int = 0;
  val resetCallCount: UIO[Unit] = ZIO.effectTotal {
    callCount = 0; ()
  }
  val getCallCount: UIO[Int] = ZIO.effectTotal(callCount)

  def plainVal: Int = {
    callCount = callCount + 1
    println(s"v1: $callCount")
    callCount
  }

  val scalaVal: Task[Int] = ZIO.effect(plainVal)

  val rand = random.nextString(2)

  val catsNow   = Eval.now(plainVal)
  val catsLater = Eval.later(plainVal)

  val scalaValMemoized = scalaVal.memoize
  val scalaValCached   = scalaVal.cached(10.seconds).flatten

  val scalaValMemoized2: ZIO[Any, Nothing, IO[Throwable, Int]] = scalaVal.memoize
  val testScalaVal = testM("scala val caching") {
    for {
      _    <- resetCallCount
      val1 <- scalaVal
      val2 <- scalaVal
    } yield assert(val1)(equalTo(val2))
  }

  val testScalaValMemoized1 = testM("scala val memoized caching [1]") {
    for {
      _    <- resetCallCount
      svm  <- scalaVal.memoize
      val1 <- svm
      val2 <- svm
    } yield assert(val1)(equalTo(val2))
  }

  val testScalaValMemoized2 = testM("scala val memoized caching [2]") {
    for {
      _    <- resetCallCount
      val1 <- scalaValMemoized
      val2 <- scalaValMemoized
    } yield assert(val1)(equalTo(val2))
  }

  val testScalaValCached1 = testM("scala val caching [1]") {
    for {
      _     <- resetCallCount
      cache <- scalaVal.cached(10.seconds)
      val1  <- cache
      val2  <- cache
    } yield assert(val1)(equalTo(val2))
  }
  val testScalaValCached2 = testM("scala val caching [2]") {
    for {
      _    <- resetCallCount
      val1 <- scalaValCached
      val2 <- scalaValCached
    } yield assert(val1)(equalTo(val2))
  }

  val t = Task {
    println(s"Compute")
    5
  }
  val test0 = testM("0") {
    for {
      memoized <- t.memoize
      tt1      <- memoized
      tt2      <- memoized
    } yield assert(memoized)(equalTo(ZIO.succeed(tt1 + tt2)))
  }

  def func(): Int = {
    println(s"Compute")
    5
  }

  val out = -4
  val eff = ZIO.succeed(out)

  import java.util.UUID

  lazy val eff0 = ZIO.effect { println("Hi"); UUID.randomUUID() }
}

import ToTest._

object LazySpec extends DefaultRunnableSpec {
  def spec = suite("ZIO val/def/memoize Specs")(
    testM("Cats Lazy eval with ZIO caches a task") {
      for {
        init <- ZIO.effect(Eval.later(plainVal))
        val1 <- Task(init)
        val2 <- Task(init)
      } yield assert(val1.value)(equalTo(val2.value))
    },
    testM("Cats Eager eval with ZIO doesn't cache a task") {
      for {
        init <- ZIO.effect(Eval.always(plainVal))
        val1 <- ZIO.effect(init.value)
        val2 <- Task(init.value)
      } yield assert(val1)(isLessThan(val2))
    },
    testM("Cats Later must cache values") {
      for {
        val1 <- Task(catsLater)
        val2 <- Task(catsLater)
      } yield assert(val1.value)(equalTo(val2.value))
    },
    test("Cats now must NOT cache values") {
      val lval1 = catsLater.value
      val lval2 = catsLater.value
      val val1  = catsNow.value
      val val2  = catsNow.value
      val val3  = catsNow.value
      println(s"val3 = $val3")
      assert(val1)(isLessThan(val2)) && assert(lval1)(isLessThan(lval2))
    }
  )
}
