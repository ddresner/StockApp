package models.dao

import java.sql.Connection

import anorm._
import javax.inject.{Inject, Singleton}
import models._
import org.postgresql.util.PSQLException
import play.Logger
import play.api.db.Database
import anorm.JodaParameterMetaData._
import org.joda.time.{DateTime, DateTimeZone}
import util.DateTimeUtils

@Singleton
class StockPricesDao @Inject() (
db: Database
) {
  private val addPrice = SQL(
    """
      | INSERT INTO stock_records (userId, stock, price, retrieved_at)
      | VALUES
      |   ({userId}, {stock}, {price}, {retrieved_at});
    """.stripMargin)

  private val delPrices = SQL(
    """
      | DELETE FROM stock_records
      | WHERE userId=(select cast (id as int) from users where username = {username}) AND stock={stock};
    """.stripMargin)

  private val indexPrices = SQL(
    """
      | SELECT userId, stock, price, retrieved_at
      | FROM stock_records
      | WHERE userId=(select cast (id as int) from users where username = {username}) AND stock={stock};
    """.stripMargin)

  private val indexLowestPrice = SQL(
    """
      | SELECT userId, stock, price, retrieved_at
      | FROM stock_records
      | WHERE userId=(select cast (id as int) from users where username = {username}) AND stock={stock}
      | ORDER BY price LIMIT 1;
    """.stripMargin)

  private val indexLowestPriceIn = SQL(
    """
      | SELECT userId, stock, price, retrieved_at
      | FROM stock_records
      | WHERE userId=(select cast (id as int) from users where username = {username}) AND stock={stock}
      | AND retrieved_at > {cap}
      | ORDER BY price LIMIT 1;
    """.stripMargin)

  private val indexHighestPrice = SQL(
    """
      | SELECT userId, stock, price, retrieved_at
      | FROM stock_records
      | WHERE userId=(select cast (id as int) from users where username = {username}) AND stock={stock}
      | ORDER BY price DESC LIMIT 1;
    """.stripMargin)

  private val indexHighestPriceIn = SQL(
    """
      | SELECT userId, stock, price, retrieved_at
      | FROM stock_records
      | WHERE userId=(select cast (id as int) from users where username = {username}) AND stock={stock}
      | AND retrieved_at > {cap}
      | ORDER BY price DESC LIMIT 1;
    """.stripMargin)

  private val indexClose = SQL(
    """
      | SELECT userId, stock, price, retrieved_at
      | FROM stock_records
      | WHERE userId=(select cast (id as int) from users where username = {username}) AND stock={stock}
      | AND retrieved_at < {cap}
      | ORDER BY retrieved_at DESC LIMIT 1;
    """.stripMargin)

  private val indexOpen = SQL(
    """
      | SELECT userId, stock, price, retrieved_at
      | FROM stock_records
      | WHERE userId=(select cast (id as int) from users where username = {username}) AND stock={stock}
      | AND retrieved_at > {cap}
      | ORDER BY retrieved_at LIMIT 1;
    """.stripMargin)

  private val indexMostRecent = SQL(
    """
      | SELECT userId, stock, price, retrieved_at
      | FROM stock_records
      | WHERE userId=(select cast (id as int) from users where username = {username}) AND stock={stock}
      | ORDER BY retrieved_at DESC LIMIT 1;
    """.stripMargin)

  def addPrice(priceRecord: StockPriceRecord): Either[String, StockPriceRecord] = {
    withDbConnection({ implicit c =>
      priceRecord match {
        case ValidStockPriceRecord(userId, stock, price, retrieved_at) =>
          addPrice.on('userId -> userId, 'stock -> stock, 'price -> price, 'retrieved_at -> retrieved_at).executeInsert[Option[Long]]() match {
            case Some(_) => Right(priceRecord)
            case None => Left("Error inserting price record")
          }
        case InvalidStockPriceRecord() => Left("Error inserting price record")
      }
    }, Left("Error inserting price record"))
  }

  def delPrices(username: String, stock: String): Either[String,Int] = {
    withDbConnection({ implicit c =>
    Right(delPrices.on("username" -> username, "stock" -> stock).executeUpdate()) },
      Left("Error deleting price record"))
  }

  def indexPrices(username: String, stock: String): Either[String, Seq[ValidStockPriceRecord]] = {
    withDbConnection({ implicit c =>
      Right(indexPrices.on("username" -> username, "stock" -> stock)
        .as(ValidStockPriceRecord.parser.*))
    }, Left("error indexing price record"))
  }

  def getLowestPriceIn(username: String, stock: String, minutes: Int): Either[String, ValidStockPriceRecord] = {
    withDbConnection({
      implicit c => minutes match {
        case 0 => Right(indexLowestPrice.on("username" -> username, "stock" -> stock)
          .as(ValidStockPriceRecord.parser.*).head)
        case _ =>
          val timeLimit:DateTime =  new DateTime().withZone(DateTimeZone.UTC).minusMinutes(minutes)
          Right(indexLowestPriceIn.on("username" -> username, "stock" -> stock, "cap" -> timeLimit)
            .as(ValidStockPriceRecord.parser.*).head)
      }
    }, Left("error indexing lowest price record"))
  }

  def getHighestPriceIn(username: String, stock: String, minutes: Int): Either[String, ValidStockPriceRecord] = {
    withDbConnection({
      implicit c => minutes match {
        case 0 => Right(indexHighestPrice.on("username" -> username, "stock" -> stock)
          .as(ValidStockPriceRecord.parser.*).head)
        case _ =>
          val timeLimit:DateTime =  new DateTime().withZone(DateTimeZone.UTC).minusMinutes(minutes)
          Right(indexHighestPriceIn.on("username" -> username, "stock" -> stock, "cap" -> timeLimit)
            .as(ValidStockPriceRecord.parser.*).head)
      }
    }, Left("error indexing highest price record"))
  }

  def getClosePrice(username: String, stock: String): Either[String, ValidStockPriceRecord] = {
    withDbConnection({
      implicit c => {
          val timeLimit:DateTime = new DateTime().withZone(DateTimeZone.UTC).withTime(0, 0, 0, 1)
        val test = indexClose.on("username" -> username, "stock" -> stock, "cap" -> timeLimit)
          .as(ValidStockPriceRecord.parser.*)
          Right(test.head)
      }
    }, Left("error indexing close price record"))
  }

  def getOpenPrice(username: String, stock: String): Either[String, ValidStockPriceRecord] = {
    withDbConnection({
      implicit c => {
        val timeLimit:DateTime = new DateTime().withZone(DateTimeZone.UTC).withTimeAtStartOfDay()
        val test = indexOpen.on("username" -> username, "stock" -> stock, "cap" -> timeLimit)
          .as(ValidStockPriceRecord.parser.*)
        Right(test.head)
      }
    }, Left("error indexing open price record"))
  }

  def getMostRecent(username: String, stock: String): Either[String, ValidStockPriceRecord] = {
    withDbConnection({ implicit c =>
      Right(indexMostRecent.on("username" -> username, "stock" -> stock)
        .as(ValidStockPriceRecord.parser.*).head)
    }, Left("error indexing price record"))
  }

  private def withDbConnection[T](f: Connection => T, failure: T): T = {
    db.withConnection { implicit c =>
      try {
        f(c)
      } catch {
        case e: Throwable =>
          Logger.error(s"[Critical] - Error executing query on stock prices table. Error $e")
          failure
      }
    }
  }
}
