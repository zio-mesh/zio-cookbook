package zio.cookbook.env

import zio.{ Task, ZIO }

object Common {
  final case class UserID(n: Int)
  final case class UserProfile(id: UserID)

}

object Database {
  import Common._

  trait Service {
    def lookup(id: UserID): Task[UserProfile]
    def update(id: UserID, profile: UserProfile): Task[Unit]
  }
}
trait Database {
  def database: Database.Service
}

object db {
  import Common._

  def lookup(id: UserID): ZIO[Database, Throwable, UserProfile] =
    ZIO.accessM(_.database.lookup(id))

  def update(id: UserID, profile: UserProfile): ZIO[Database, Throwable, Unit] =
    ZIO.accessM(_.database.update(id, profile))
}
