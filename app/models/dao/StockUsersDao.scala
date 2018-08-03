package models.dao

import java.sql.{Connection, ResultSet}

import anorm._
import javax.inject.{Inject, Singleton}
import models.{StockFavorite, StockUser}
import org.postgresql.util.PSQLException
import play.Logger
import play.api.db.Database

@Singleton
class StockUsersDao @Inject()(
  db: Database
) {
  private val addUser: SqlQuery = SQL(
    """
      | INSERT INTO users (username)
      | VALUES
      |   ({username})
      | RETURNING *;
    """.stripMargin)

  private val delUser = SQL(
    """
      | DELETE FROM users
      | WHERE username={username}
    """.stripMargin)

  private val idxUser = SQL(
    """
      | SELECT *
      | FROM users
      | WHERE username={username};
    """.stripMargin)

  private val idxAllUsers = SQL(
    """
      | SELECT *
      | FROM users
    """.stripMargin)

  def addUser(username:String): Either[String, StockUser] = {
    withDbConnection({
      implicit c => {
        Right(addUser.on("username" -> username).as(StockUser.parser.*).head)
      }
    }, Left("Error inserting user"))
  }

  def delUser(username:String): Either[String, Int] = {
    withDbConnection({ implicit c =>
      Right(delUser.on("username" -> username).executeUpdate())
    }, Left("Error deleting user"))
  }

  def findByUsername(username: String): Either[String, StockUser] = {
    withDbConnection( { implicit c =>
      Right(idxUser.on("username"  -> username).as(StockUser.parser.*).head)
    }, Left("Error indexing user"))
  }

  def indexAllUsers(): Either[String, Seq[StockUser]] = {
    withDbConnection( { implicit c =>
      Right(idxAllUsers.as(StockUser.parser.*))
    }, Left("Error indexing all users"))
  }

  private def withDbConnection[T](f: Connection => T, failure: T): T = {
    db.withConnection { implicit c =>
      try {
        f(c)
      } catch {
        case e: PSQLException =>
          Logger.error(s"[Critical] - Error executing query on users table. Error $e")
          failure
        case e: Throwable => throw e
      }
    }
  }

}
