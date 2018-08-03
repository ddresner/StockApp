package controllers

import models.{StockFavorite, StockUser, ValidStockPriceRecord}
import models.dao.StockFavoritesDao
import org.scalatestplus.play.{PlaySpec, _}
import org.scalatest._
import Matchers._
import anorm.Success
import clients.StockExchangeClient
import util.StockExchangeService

import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration
import scala.util.Failure

class StockExchangeControllerSpec extends PlaySpec with OneAppPerSuite {

//  "post lol" in {
//    val service = app.injector.instanceOf[StockExchangeService]
//    service.addFavorite(StockFavorite("lol", "msft")) match {
//      case Left(error) => println(error)
//      case Right(fav) => println(fav.userId + " " + fav.stock)
//    }
//  }
//
//  "delete lol" in {
//    val service = app.injector.instanceOf[StockExchangeService]
//    service.delFavorite(StockFavorite("lol", "msft")) match {
//      case Left(error) => println(error)
//      case Right(int) => println(int)
//    }
//  }
//
//  "retrieve lol's favorites" in {
//    val service = app.injector.instanceOf[StockExchangeService]
//    service.findFavorites(StockUser("lol")) match {
//      case Left(error) => println(error)
//      case Right(seq) => println(seq.toString())
//    }
//  }
//
//  "add current stock price" in {
//    val service = app.injector.instanceOf[StockExchangeService]
//    service.addCurrentPriceRecord(StockFavorite("lol","msft")) match {
//      case Right(ValidStockPriceRecord(a,b,c,d)) => println(a+" "+b+" "+c+" "+d)
//      case Left(error) => println(error)
//    }
//  }
//
//  "get lol's data for stock" in {
//    val service = app.injector.instanceOf[StockExchangeService]
//    service.getAllRecordsForFav(StockFavorite("lol","fb")) match {
//      case Right(seq) => seq.foreach(record => {
//        val ValidStockPriceRecord(a,b,c,d) = record
//        println(a+" "+b+" "+c+" "+d)
//      })
//      case Left(error) => println(error)
//    }
//  }
//
//  "delete lol's stock records for stock" in {
//    val service = app.injector.instanceOf[StockExchangeService]
//    service.delPriceRecords(StockFavorite("lol","fb")) match {
//      case Right(int) => println(int)
//      case Left(error) => println(error)
//    }
//  }
//
//  "getting open for lol's stock" in {
//    val service = app.injector.instanceOf[StockExchangeService]
//    service.getCurrentDayChange(StockFavorite("lol","fb")) match {
//      case Right(double) => println(double)
//      case Left(error) => println(error)
//    }
//  }
}
