package util

import akka.actor.ActorSystem
import clients.StockExchangeClient
import javax.inject.{Inject, Singleton}
import models._
import models.dao._
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent._
import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration


trait StockExchangeService {

  val system: ActorSystem = ActorSystem.create("StockRequestsSystem")

  def addUser(username: String): Either[String, StockUser]
  def delUser(username: String): Either[String, Int]
  def findUser(username: String): Either[String, StockUser]
  def allUsers():Either[String, Seq[StockUser]]

  def addFavorite(username: String, stock:String): Either[String, StockFavorite]
  def delFavorite(username: String, stock:String): Either[String, Int]
  def findFavorites(username: String): Either[String, Seq[StockFavorite]]
  def allFavorites(): Either[String, Seq[StockFavorite]]

  def addCurrentPriceRecord(favorite: StockFavorite):Either[String, StockPriceRecord]

  def delPriceRecords(username:String, stock:String): Either[String,Int]
  def getAllRecordsForFav(username:String, stock:String): Either[String, Seq[ValidStockPriceRecord]]

  def getLowestPriceIn(username:String, stock:String, minutes: Int): Either[String, ValidStockPriceRecord]
  def getHighestPriceIn(username:String, stock:String, minutes: Int): Either[String, ValidStockPriceRecord]

  def getCurrentDayChange(username:String, stock:String): Either[String, DayChange]
}


@Singleton
class DefaultStockExchangeService @Inject() (
  stockExchangeClient: StockExchangeClient,
  stockFavoritesDao:StockFavoritesDao,
  stockPricesDao: StockPricesDao,
  stockUsersDao: StockUsersDao
) extends StockExchangeService {

  override def addUser(username: String): Either[String, StockUser] = {
    stockUsersDao.addUser(username)
  }
  override def delUser(username: String): Either[String, Int] = {
    findFavorites(username) match {
      case Right(seq) =>
        if(seq.foldLeft(true)((bool:Boolean,stockFav:StockFavorite) => {
          delFavorite(username, stockFav.stock) match {
            case Right(_) => bool
            case Left(_) => false
          }
        }))
          stockUsersDao.delUser(username)
        else
          Left("Error deleting user " + username)

      case Left(err) => Left(err)
    }
  }
  override def findUser(username: String): Either[String, StockUser] = {
    stockUsersDao.findByUsername(username)
  }
  override def allUsers(): Either[String, Seq[StockUser]] = {
    stockUsersDao.indexAllUsers()
  }

  override def addFavorite(username: String, stock: String): Either[String, StockFavorite] = {
    val future = stockFavoritesDao.addFavorite(username,stock)
    future match {
      case Right(stockFavorite) => addCurrentPriceRecord(stockFavorite)
      case _ =>
    }
    future
  }
  override def delFavorite(username: String, stock: String): Either[String, Int] = {
    delPriceRecords(username, stock)
    stockFavoritesDao.delFavorite(username,stock)
  }

  override def findFavorites(username: String): Either[String, Seq[StockFavorite]] = {
    stockFavoritesDao.indexFavorite(username)
  }
  override def allFavorites(): Either[String, Seq[StockFavorite]] = {
    stockFavoritesDao.indexAllFavorites()
  }

  override def addCurrentPriceRecord(favorite: StockFavorite): Either[String, StockPriceRecord] = {
    val priceRecord = stockExchangeClient.getCurrentStockPrice(favorite)
    val result = Await.result(priceRecord, FiniteDuration(5,"second"))
    result match{
      case ValidStockPriceRecord(_,_,_,_) => stockPricesDao.addPrice(result)
      case _ => Left("Error inserting price record")
    }
  }

  override def delPriceRecords(username: String, stock: String): Either[String, Int] = {
    stockPricesDao.delPrices(username, stock)
  }
  override def getAllRecordsForFav(username: String, stock: String): Either[String, Seq[ValidStockPriceRecord]] = {
    stockPricesDao.indexPrices(username, stock)
  }

  override def getLowestPriceIn(username: String, stock: String, minutes: Int): Either[String, ValidStockPriceRecord] = {
    stockPricesDao.getLowestPriceIn(username, stock, minutes)
  }
  override def getHighestPriceIn(username: String, stock: String, minutes: Int): Either[String, ValidStockPriceRecord] = {
    stockPricesDao.getHighestPriceIn(username, stock, minutes)
  }
  override def getCurrentDayChange(username: String, stock: String): Either[String, DayChange] = {
    val mostRecent = stockPricesDao.getMostRecent(username, stock)
    mostRecent match {
      case Right(ValidStockPriceRecord(_, _, current, _)) =>
        val closePrice = stockPricesDao.getClosePrice(username, stock)
        closePrice match {
          case Right(ValidStockPriceRecord(_, _, close, _)) =>
            val currDayChange = BigDecimal(current - close).setScale(3, BigDecimal.RoundingMode.HALF_UP).toDouble
            val percentage = BigDecimal(Math.abs(currDayChange*100)/close)
              .setScale(3, BigDecimal.RoundingMode.HALF_UP).toDouble
            Right(DayChange(currDayChange, percentage))
          case _ => Left("Error getting close price record for " + stock)
        }
      case _ => Left("Error getting most recent price record for " + stock)
    }
  }
}

@Singleton
class MockStockExchangeService @Inject() (
  stockExchangeClient: StockExchangeClient
) extends StockExchangeService {

  private val stockExchangeUserData = mutable.Map[String,Int]()
  private var idCounter = 0

  private val stockExchangeFavData = mutable.Map[StockFavorite,Boolean]()

  private val stockExchangePriceData = mutable.Map[StockFavorite, mutable.Map[ValidStockPriceRecord,Boolean]]()

  override def addUser(username:String): Either[String,StockUser] = {
    val stockUser = StockUser(idCounter, username)
    stockExchangeUserData.put(username, idCounter)
    idCounter = idCounter + 1
    Right(stockUser)
  }

  override def delUser(username: String): Either[String, Int] = {
    stockExchangeUserData -= username
    Right(1)
  }

  override def findUser(username: String): Either[String, StockUser] = {
    Right(StockUser(stockExchangeUserData(username),username))
  }

  override def allUsers(): Either[String, Seq[StockUser]] = {
    Right(stockExchangeUserData.toList.foldLeft(List[StockUser]())((list,pair) =>
      list :+ StockUser(pair._2,pair._1)))
  }

  override def addFavorite(username:String, stock:String): Either[String,StockFavorite] = {
    val id = stockExchangeUserData.getOrElse(username, Int.MinValue)
    if(id == Int.MinValue)
      Left("Error adding favorite because: user does not exist")
    else{
      val favorite = StockFavorite(id, username)
      stockExchangeFavData.put(favorite,true)
      Right(favorite)
    }
  }

  override def delFavorite(username:String, stock:String): Either[String,Int] = {
    val id = stockExchangeUserData.getOrElse(username, Int.MinValue)
    if(id == Int.MinValue)
      Left("Error adding favorite because: user does not exist")
    else{
      val favorite = StockFavorite(id, username)
      stockExchangeFavData -= favorite
      Right(1)
    }

  }

  override def findFavorites(username:String): Either[String,Seq[StockFavorite]] = {
    val id = stockExchangeUserData.getOrElse(username, Int.MinValue)
    if(id == Int.MinValue)
      Left("Error adding favorite because: user does not exist")
    else{
      Right(stockExchangeFavData.keys.foldLeft(Seq[StockFavorite]())((list,fav) => {
        if(fav.userId == id)
          list :+ fav
        else
          list
      }))
    }
  }

  override def allFavorites(): Either[String, Seq[StockFavorite]] = {
    Right(stockExchangeFavData.keys.toList)
  }

  override def addCurrentPriceRecord(favorite: StockFavorite): Either[String,StockPriceRecord] = {
    val priceRecord = stockExchangeClient.getCurrentStockPrice(favorite)
    val result = Await.result(priceRecord, FiniteDuration(5, "seconds"))
    result match {
      case ValidStockPriceRecord(userId,stock,price,time) =>
        val valMap = stockExchangePriceData.getOrElse(favorite, mutable.Map[ValidStockPriceRecord, Boolean]())
        valMap.put(ValidStockPriceRecord(userId, stock, price, time), true)
        stockExchangePriceData.put(favorite, valMap)
        Right(result)
      case _ => Left("Error inserting price record")
    }
  }

  override def delPriceRecords(username:String, stock:String): Either[String, Int] = {
    val id = stockExchangeUserData.getOrElse(username, Int.MinValue)
    if(id == Int.MinValue)
      Left("Error deleting price record because: user does not exist")
    else{
      val favorite = StockFavorite(id, stock)
      stockExchangePriceData -= favorite
      Right(1)
    }
  }

  override def getAllRecordsForFav(username:String, stock:String): Either[String, Seq[ValidStockPriceRecord]] = {
    val id = stockExchangeUserData.getOrElse(username, Int.MinValue)
    if(id == Int.MinValue)
      Left("Error getting price records because: user does not exist")
    else{
      val favorite = StockFavorite(id, stock)
      Right(stockExchangePriceData.getOrElse(favorite,  mutable.Map[ValidStockPriceRecord,Boolean]()).keys.toList)
    }
  }

  override def getLowestPriceIn(username: String, stock: String, months: Int): Either[String, ValidStockPriceRecord] = {
    val id = stockExchangeUserData.getOrElse(username, Int.MinValue)
    if(id == Int.MinValue)
      Left("Error getting price records because: user does not exist")
    else{
      val favorite = StockFavorite(id, stock)
      months match {
        case 0 => Right(stockExchangePriceData(favorite).keys.toList.minBy(record => record.price))
        case _ =>
          val timeLimit:DateTime =  new DateTime().minusMonths(months)
          Right(extract(stockExchangePriceData(favorite).keys.toList)(record => record.retrieved_at.isAfter(timeLimit))
          .minBy(record => record.price))
      }
    }
  }
  override def getHighestPriceIn(username: String, stock: String, months: Int): Either[String, ValidStockPriceRecord] = {
    val id = stockExchangeUserData.getOrElse(username, Int.MinValue)
    if(id == Int.MinValue)
      Left("Error getting price records because: user does not exist")
    else{
      val favorite = StockFavorite(id, stock)
      months match {
        case 0 => Right(stockExchangePriceData(favorite).keys.toList.maxBy(record => record.price))
        case _ =>
          val timeLimit:DateTime =  new DateTime().minusMonths(months)
          Right(extract(stockExchangePriceData(favorite).keys.toList)(record => record.retrieved_at.isAfter(timeLimit))
            .maxBy(record => record.price))
      }
    }
  }

  private def extract(_xs: List[ValidStockPriceRecord])(cond: ValidStockPriceRecord => Boolean): List[ValidStockPriceRecord] = {
    def inner(xs: List[ValidStockPriceRecord], res: List[ValidStockPriceRecord]): List[ValidStockPriceRecord] = xs match {
      case Nil => Nil
      case x :: y :: tail if cond(y) && res.isEmpty => inner(tail, res ++ (x :: y :: Nil))
      case x :: y :: tail if cond(x) && res.nonEmpty => res ++ (x :: y :: Nil)
      case x :: tail if res.nonEmpty => inner(tail, res :+ x)
      case x :: tail => inner(tail, res)
    }

    inner(_xs, Nil)
  }

  override def getCurrentDayChange(username:String, stock:String): Either[String, DayChange] = {
    val id = stockExchangeUserData.getOrElse(username, Int.MinValue)
    if(id == Int.MinValue)
      Left("Error getting current day change because: user does not exist")
    else{
      val favorite = StockFavorite(id, stock)
      val priceRecord = stockExchangeClient.getCurrentStockPrice(favorite)
      val result = Await.result(priceRecord, FiniteDuration(5,"second"))
      result match{
        case ValidStockPriceRecord(_,_,current,_) =>
          val closePrice = extract(stockExchangePriceData(favorite).keys.toList)(record =>
            record.retrieved_at.isBefore(new DateTime().withZone(DateTimeZone.UTC).withTimeAtStartOfDay()))
            .maxBy(record => record.retrieved_at.getMillis)
          closePrice match {
            case ValidStockPriceRecord(_,_,close,_) =>
              val currDayChange = current - close
              Right(DayChange(currDayChange, (currDayChange*100)/close))
            case _ => Left("Error getting open price record for " + favorite.stock)
          }
        case _ => Left("Error getting current price record for " + favorite.stock)
      }
    }
  }
}


