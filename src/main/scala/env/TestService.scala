// Implement a mock DB service

package zio.cookbook.env

import zio.cookbook.env.Common._
import zio.{ Task }

class TestService extends Database.Service {
  private var map: Map[UserID, UserProfile] = Map()

  def setTestData(map0: Map[UserID, UserProfile]): Task[Unit] =
    Task { map = map0 }

  def getTestData: Task[Map[UserID, UserProfile]] =
    Task(map)

  def lookup(id: UserID): Task[UserProfile] =
    Task(map(id))

  def update(id: UserID, profile: UserProfile): Task[Unit] =
    Task.effect { map = map + (id -> profile) }
}

object TestService extends TestService

trait TestDatabase extends Database {
  val database: TestService = new TestService
}
object TestDatabase extends TestDatabase
