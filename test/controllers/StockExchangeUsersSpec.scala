package controllers


import org.scalatestplus.play.{PlaySpec, _}
import util.StockExchangeService


class StockExchangeUsersSpec extends PlaySpec with OneAppPerSuite {

  "new user" in {
    val service = app.injector.instanceOf[StockExchangeService]
    service.addUser("dannys") match {
      case Left(error) => println(error)
      case Right(user) => println(user.id + " " + user.username)
    }
  }


}
