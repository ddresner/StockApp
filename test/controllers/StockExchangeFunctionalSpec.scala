package controllers

import org.scalatestplus.play.{OneServerPerTest, PlaySpec}
import play.api.libs.ws.WSClient
import play.mvc.Http
import play.api.test.Helpers

import scala.concurrent.duration._
import akka.util.Timeout
import models.{StockFavorite, StockUser, ValidStockPriceRecord}
import models.StockUser.stockUserReads
import models.dao.{StockFavoritesDao, StockUsersDao}


import scala.util.Random



class StockExchangeFunctionalSpec extends PlaySpec with OneServerPerTest {

  private val EmptyBody = ""
  implicit val duration: Timeout = 20 seconds
  lazy val BaseUrl = s"http://localhost:$port"
  lazy val usersDao: StockUsersDao = app.injector.instanceOf[StockUsersDao]
  lazy val favoritesDao: StockFavoritesDao = app.injector.instanceOf[StockFavoritesDao]

  "test server logic" must {
    "test add/delete user" in {
      val wsClient = app.injector.instanceOf[WSClient]
      val username = createRandomString
      val addResponse = Helpers.await(wsClient.url(BaseUrl + "/addUser/" + username).post(EmptyBody))
      addResponse.status mustBe Http.Status.OK

      val user = addResponse.json.as[StockUser]
      user.username mustBe username

      usersDao.findByUsername(username) match {
        case Right(foundUser) =>
          foundUser mustBe user
        case Left(errors) => fail(errors)
      }

      val delResponse = Helpers.await(wsClient.url(BaseUrl + "/deleteUser/" + username).delete())
      delResponse.status mustBe Http.Status.OK
    }

    "test add/get/delete favorite" in {
      val wsClient = app.injector.instanceOf[WSClient]
      val username = createRandomString
      val addUserResponse = Helpers.await(wsClient.url(BaseUrl + "/addUser/" + username).post(EmptyBody))
      addUserResponse.status mustBe Http.Status.OK

      val addFavResponse = Helpers.await(wsClient.url(BaseUrl +
        "/users/" + username + "/addFavoriteStock/msft").post(EmptyBody))
      addFavResponse.status mustBe Http.Status.OK

      val getFavResponse = Helpers.await(wsClient.url(BaseUrl + "/users/" + username).get())
      getFavResponse.status mustBe Http.Status.OK

      val favs = getFavResponse.json.as[Seq[StockFavorite]]
      favoritesDao.indexFavorite(username) match {
        case Right(foundFavs) => foundFavs mustBe favs
        case Left(errors) => fail(errors)
      }

      val delFavResponse = Helpers.await(wsClient.url(BaseUrl +
        "/users/" + username + "/deleteFavoriteStock/msft").delete())
      delFavResponse.status mustBe Http.Status.OK

      val getFavResponse2 = Helpers.await(wsClient.url(BaseUrl + "/users/" + username).get())
      getFavResponse2.status mustBe Http.Status.OK

      getFavResponse2.json.as[Seq[StockFavorite]].toList.size mustBe 0

      val delUserResponse = Helpers.await(wsClient.url(BaseUrl + "/deleteUser/" + username).delete())
      delUserResponse.status mustBe Http.Status.OK
    }

    "test add/get/delete price data records" in {
      val wsClient = app.injector.instanceOf[WSClient]
      val username = createRandomString
      val addUserResponse = Helpers.await(wsClient.url(BaseUrl + "/addUser/" + username).post(EmptyBody))
      addUserResponse.status mustBe Http.Status.OK

      val addFavResponse = Helpers.await(wsClient.url(BaseUrl +
        "/users/" + username + "/addFavoriteStock/googl").post(EmptyBody))
      addFavResponse.status mustBe Http.Status.OK

      val getPriceResponse = Helpers.await(wsClient.url(BaseUrl +
        "/users/" + username + "/stockRecords/googl").get()).json.as[Seq[ValidStockPriceRecord]]
      getPriceResponse.isEmpty mustBe false

      val delPriceResponse = Helpers.await(wsClient.url(BaseUrl +
        "/users/" + username + "/deleteRecords/googl").delete())
      delPriceResponse.status mustBe Http.Status.OK

      val getPriceResponse2 = Helpers.await(wsClient.url(BaseUrl +
        "/users/" + username + "/stockRecords/googl").get()).json.as[Seq[ValidStockPriceRecord]]
      getPriceResponse2.isEmpty mustBe true

      val delUserResponse = Helpers.await(wsClient.url(BaseUrl + "/deleteUser/" + username).delete())
      delUserResponse.status mustBe Http.Status.OK
    }

    "test deleting user also deletes data and favorites" in {
      val wsClient = app.injector.instanceOf[WSClient]
      val username = createRandomString
      val addUserResponse = Helpers.await(wsClient.url(BaseUrl + "/addUser/" + username).post(EmptyBody))
      addUserResponse.status mustBe Http.Status.OK

      val addFavResponse = Helpers.await(wsClient.url(BaseUrl +
        "/users/" + username + "/addFavoriteStock/amzn").post(EmptyBody))
      addFavResponse.status mustBe Http.Status.OK

      val delUserResponse = Helpers.await(wsClient.url(BaseUrl + "/deleteUser/" + username).delete())
      delUserResponse.status mustBe Http.Status.OK

      val getFavResponse = Helpers.await(wsClient.url(BaseUrl + "/users/" + username).get())
      getFavResponse.status mustBe Http.Status.OK
      getFavResponse.json.as[Seq[StockFavorite]].isEmpty mustBe true

      val getPriceResponse = Helpers.await(wsClient.url(BaseUrl +
        "/users/" + username + "/stockRecords/googl").get()).json.as[Seq[ValidStockPriceRecord]]
      getPriceResponse.isEmpty mustBe true
    }

     "day change" in {
       val wsClient = app.injector.instanceOf[WSClient]
       val dayChangeResponse = Helpers.await(wsClient.url(BaseUrl + "/users/ashley/dayChange/msft").get())

     }
  }

  private def createRandomString: String = {
    Random.alphanumeric.take(30).mkString
  }
}
