package controllers

import clients.StockExchangeClient
import javax.inject.{Inject, Singleton}
import models.{StockFavorite, StockUser}
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc._
import util.StockExchangeService

import scala.concurrent.Future



@Singleton
class StockExchangeController @Inject() (
  stockExchangeService: StockExchangeService
) extends Controller {

//  def get(stockSymbol: String) = Action { _ =>
//        Ok(Json.toJson(stockExchangeService.get(stockSymbol)))
////    Future {
////      Ok("")
////    }
//  }

  def postUser(username: String) = Action {
    stockExchangeService.addUser(username) match {
      case Left(error) => ExpectationFailed(Json.toJson(error))
      case Right(user) => Ok(Json.toJson(user))
    }
  }

  def deleteUser(username: String) = Action {
    stockExchangeService.delUser(username) match {
      case Left(error) => ExpectationFailed(Json.toJson(error))
      case Right(fav) => Ok(Json.toJson(fav))
    }
  }

  def postFavorite(username: String, stockSymbol: String) = Action {
    stockExchangeService.addFavorite(username, stockSymbol) match {
      case Left(error) => ExpectationFailed(Json.toJson(error))
      case Right(fav) => Ok(Json.toJson(fav))
    }
  }

  def deleteFavorite(username: String, stockSymbol: String) = Action {
    stockExchangeService.delFavorite(username, stockSymbol) match {
      case Left(error) => ExpectationFailed(Json.toJson(error))
      case Right(fav) => Ok(Json.toJson(fav))
    }
  }

  def findById(username: String) = Action {
    stockExchangeService.findFavorites(username) match {
      case Left(error) => ExpectationFailed(Json.toJson(error))
      case Right(favList) => Ok(Json.toJson(favList))
    }
  }

  def deleteRecords(username: String, stockSymbol: String) = Action {
    stockExchangeService.delPriceRecords(username, stockSymbol) match {
      case Left(error) => ExpectationFailed(Json.toJson(error))
      case Right(delFlag) => Ok(Json.toJson(delFlag))
    }
  }

  def getRecords(username: String, stockSymbol: String) = Action {
    stockExchangeService.getAllRecordsForFav(username, stockSymbol) match {
      case Left(error) => ExpectationFailed(Json.toJson(error))
      case Right(recList) => Ok(Json.toJson(recList))
    }
  }

  def getLowestPrice(username: String, stockSymbol: String): Action[AnyContent] = getLowestPriceIn(username, stockSymbol)

  def getLowestPriceIn(username: String, stockSymbol: String, minutes:Int = 0) = Action {
    stockExchangeService.getLowestPriceIn(username, stockSymbol, minutes) match {
      case Left(error) => ExpectationFailed(Json.toJson(error))
      case Right(price) => Ok(Json.toJson(price))
    }
  }

  def getHighestPrice(username: String, stockSymbol: String): Action[AnyContent] = getHighestPriceIn(username, stockSymbol)

  def getHighestPriceIn(username: String, stockSymbol: String, minutes:Int = 0) = Action {
    stockExchangeService.getHighestPriceIn(username, stockSymbol, minutes) match {
      case Left(error) => ExpectationFailed(Json.toJson(error))
      case Right(price) => Ok(Json.toJson(price))
    }
  }

  def getDayChange(username: String, stockSymbol:String) = Action {
    stockExchangeService.getCurrentDayChange(username, stockSymbol) match {
      case Left(error) => ExpectationFailed(Json.toJson(error))
      case Right(change) => Ok(Json.toJson(change))
    }
  }


}
