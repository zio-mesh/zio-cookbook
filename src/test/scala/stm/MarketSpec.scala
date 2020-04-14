package zio.cookbook.stm

import helper._

import zio.cookbook.env.Common.{ UserID, UserProfile }
import zio.cookbook.env.TestService._
import zio.stm.{ STM, TRef }
import zio.test.Assertion.{ equalTo }
import zio.test._
import zio.{ Schedule, ZIO }

object MarketSpec extends DefaultRunnableSpec {
  def spec = suite("STMSpec")(
    suite("Dangerous programming spec")(
      testM("Multiple fibers to lookup the same entry") {
        for {
          _   <- setTestData(Map(user0 -> prof0))
          req <- ZIO.forkAll(List.fill(agents)(lookup(user0))) // spawn fibers
          res <- req.join // read result from all fibers
        } yield assert(res)(equalTo(initList))
      }
    ),
    suite("STM atomic programming spec")(
      testM("Multiple fibers to lookup the same entry") {
        for {
          tref <- TRef.makeCommit(prof0)
          _    <- setTestData(Map(user0 -> prof0))
          req  <- ZIO.forkAll(List.fill(agents)(safeLookup(tref, retries))) // spawn fibers
          res  <- req.join // read result from all fibers
        } yield assert(res)(equalTo(initList))
      }
    )
  )
}

object helper {
  val user0 = UserID(0)
  val prof0 = UserProfile(user0)

  val agents   = 2
  val retries  = 3
  val initList = List.fill(agents)(prof0)

  // Use STM Monad to atomically serialize RD/WR access to the shared resource
  def safeLookup(tvar: TRef[UserProfile], n: Int) =
    STM
      .atomically(for {
        v <- tvar.get
      } yield v)
      .repeat(Schedule.recurs(n) *> Schedule.identity)

}
