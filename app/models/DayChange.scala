package models

import anorm.{Macro, RowParser}
import play.api.libs.functional.syntax.{unlift, _}
import play.api.libs.json.{JsPath, Reads, Writes}

object DayChange extends Enumeration {
  implicit val dayChangeWrites: Writes[DayChange] = (
    (JsPath \ "change").write[Double] and
      (JsPath \ "percentage").write[Double]
    )(unlift(DayChange.unapply))

  implicit val dayChangeReads: Reads[DayChange] = (
    (JsPath \ "change").read[Double] and
      (JsPath \ "percentage").read[Double]
    )(DayChange.apply _)

  val parser: RowParser[DayChange] = Macro.namedParser[DayChange]
}

case class DayChange(change: Double, percentage: Double)
