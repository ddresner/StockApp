package models.dao

import java.sql.Connection

import anorm._
import javax.inject.{Inject, Singleton}
import models.{StockFavorite, StockUser}
import org.postgresql.util.PSQLException
import play.Logger
import play.api.db.Database

@Singleton
class StockFavoritesDao @Inject() (
  db: Database
) {
  private val addFav: SqlQuery = SQL(
    """
      | insert into favorite_stocks (userId, stock)
      | values ((select cast (id as int) from users where username = {username}),{stock})
      | returning *
    """.stripMargin)

  private val delFav = SQL(
    """
      | DELETE FROM favorite_stocks
      | WHERE userId=(select cast (id as int) from users where username = {username}) AND stock={stock}
    """.stripMargin)

  private val indexFav = SQL(
    """
      | select * from favorite_stocks
      | where userid = (select cast (id as int)
      | from users
      | where username = {username} )
    """.stripMargin)

  private val indexAllFavs = SQL(
    """
      | SELECT *
      | FROM favorite_stocks
    """.stripMargin)

  def addFavorite(username: String, stock:String): Either[String, StockFavorite] = {
    withDbConnection({ implicit c =>
      Right(addFav.on("username" -> username, "stock" -> stock).as(StockFavorite.parser.*).head)
    }, Left("Error inserting favorite"))
  }

  def delFavorite(username: String, stock:String): Either[String, Int] = {
    withDbConnection({ implicit c =>
      Right(delFav.on("username" -> username, "stock" -> stock).executeUpdate())
    }, Left("Error deleting favorite"))
  }

  def indexFavorite(username: String): Either[String, Seq[StockFavorite]] = {
    withDbConnection( { implicit c =>
      Right(indexFav.on("username"  -> username).as(StockFavorite.parser.*))
    }, Left("Error indexing favorite"))
  }

  def indexAllFavorites(): Either[String, Seq[StockFavorite]] = {
    withDbConnection( { implicit c =>
      Right(indexAllFavs.as(StockFavorite.parser.*))
    }, Left("Error indexing all favorites"))
  }

  private def withDbConnection[T](f: Connection => T, failure: T): T = {
    db.withConnection { implicit c =>
      try {
       f(c)
      } catch {
        case e: PSQLException =>
          Logger.error(s"[Critical] - Error executing query on favorites table. Error $e")
          failure
        case e: Throwable => throw e
      }
    }
  }

}
