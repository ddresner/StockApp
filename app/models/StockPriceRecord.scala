package models

import java.sql.Timestamp

import anorm.{Macro, RowParser}
import org.joda.time.{DateTime, DateTimeZone, LocalDateTime}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.libs.functional.syntax._
import play.api.libs.json._

object ValidStockPriceRecord {
  implicit val validStockPriceRecordWrites: Writes[ValidStockPriceRecord] = (
    (JsPath \ "userId").write[Int] and
      (JsPath \ "stock").write[String] and
        (JsPath \ "price").write[Double] and
      (JsPath \ "priceTime").write[DateTime]
    )(unlift(ValidStockPriceRecord.unapply))

  implicit val validStockPriceRecordReads: Reads[ValidStockPriceRecord] = (
    (JsPath \ "userId").read[Int] and
      (JsPath \ "stock").read[String] and
      (JsPath \ "price").read[Double] and
      (JsPath \ "priceTime").read[DateTime]
    )(ValidStockPriceRecord.apply _)

  val parser: RowParser[ValidStockPriceRecord] = Macro.namedParser[ValidStockPriceRecord]
}

trait StockPriceRecord



case class ValidStockPriceRecord(userId: Int, stock:String, price: Double,
  retrieved_at: DateTime = new DateTime().withZone(DateTimeZone.UTC))
  extends StockPriceRecord

case class InvalidStockPriceRecord() extends StockPriceRecord