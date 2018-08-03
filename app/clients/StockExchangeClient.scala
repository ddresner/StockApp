package clients


import scala.util.{Failure, Success}
import javax.inject.{Inject, Singleton}
import models._
import play.api.Logger
import play.api.libs.ws.{WSClient, WSResponse}

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.control.Exception
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.parsing.json.JSON

trait StockExchangeClient {

  def getCurrentStockPrice(stockFavorite: StockFavorite): Future[StockPriceRecord]
}


@Singleton
class DefaultStockExchangeClient @Inject()(
  ws: WSClient
) extends StockExchangeClient {

  val BaseUrl = "https://api.iextrading.com/1.0/stock/"

  override def getCurrentStockPrice(favorite: StockFavorite): Future[StockPriceRecord] = {
    // Make http request and handle errors

    val request = ws.url(BaseUrl + favorite.stock + "/price")
    val price = request.get()
    price.map { wsResponse =>
      ValidStockPriceRecord(favorite.userId, favorite.stock, wsResponse.body.toDouble)
    }.recover {
      case e: Throwable =>
        Logger.error("Could not get stock price record for " + favorite.stock)
        InvalidStockPriceRecord()
    }
  }
}


@Singleton
class MockStockExchangeClient @Inject()(

) extends StockExchangeClient {

  private val stockPriceData = mutable.Map[StockFavorite, StockPriceRecord]()

  def setStockPrice(userId: Int, stockSymbol: String, stock: StockPriceRecord): Unit = {
    stockPriceData.put(StockFavorite(userId, stockSymbol), stock)
  }

  override def getCurrentStockPrice(favorite: StockFavorite): Future[StockPriceRecord] = {
    Future.successful(stockPriceData.getOrElse(favorite, InvalidStockPriceRecord()))
  }
}


