package models

import anorm.{Macro, RowParser}
import play.api.libs.functional.syntax.{unlift, _}
import play.api.libs.json.{JsPath, Reads, Writes}

object StockUser {
  implicit val stockUserWrites: Writes[StockUser] = (
    (JsPath \ "id").write[Int] and
      (JsPath \ "username").write[String]
    )(unlift(StockUser.unapply))

  implicit val stockUserReads: Reads[StockUser] = (
    (JsPath \ "id").read[Int] and
      (JsPath \ "username").read[String]
    )(StockUser.apply _)

  val parser: RowParser[StockUser] = Macro.namedParser[StockUser]
}

case class StockUser(id: Int, username:String)

