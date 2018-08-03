package models

import anorm.{ Macro, RowParser }
import play.api.libs.functional.syntax._
import play.api.libs.json._


trait Stock

object ValidStock {
  implicit val validStockWrites: Writes[ValidStock] = (
    (JsPath \ "symbol").write[String] and
      (JsPath \ "companyName").write[String] and
        (JsPath \ "exchange").write[String] and
      (JsPath \ "industry").write[String] and
      (JsPath \ "website").write[String] and
      (JsPath \ "description").write[String] and
      (JsPath \ "CEO").write[String] and
      (JsPath \ "issueType").write[String] and
      (JsPath \ "sector").write[String]
    )(unlift(ValidStock.unapply))

  val parser: RowParser[ValidStock] = Macro.namedParser[ValidStock]
}

case class ValidStock(symbol:String, companyName:String, exchange:String, industry:String, website:String,
                 description:String, CEO:String, issueType:String, sector:String) extends Stock

case class InvalidStock() extends Stock

object StockFavorite {
  implicit val stockFavoriteWrites: Writes[StockFavorite] = (
    (JsPath \ "userId").write[Int] and
      (JsPath \ "stock").write[String]
    )(unlift(StockFavorite.unapply))

  implicit val stockFavoriteReads: Reads[StockFavorite] = (
    (JsPath \ "userId").read[Int] and
      (JsPath \ "stock").read[String]
    )(StockFavorite.apply _)

  val parser: RowParser[StockFavorite] = Macro.namedParser[StockFavorite]
}

case class StockFavorite(userId: Int, stock: String)

