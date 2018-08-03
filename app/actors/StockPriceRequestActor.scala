package actors

import java.time.DayOfWeek

import akka.actor.{Actor, Props}
import util.StockExchangeService
import javax.inject.{Inject, Singleton}
import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

object StockPriceRequestActor {
  def props(service:StockExchangeService): Props = Props(new StockPriceRequestActor(service))
}


@Singleton
class StockPriceRequestActor @Inject() (service: StockExchangeService) extends Actor {
  private case object RequestBang

  context.system.scheduler.schedule(FiniteDuration(0,"seconds"),FiniteDuration(5,"seconds"),self,RequestBang)

  def receive:PartialFunction[Any, Unit] = {
    case RequestBang =>
      val dateTime = DateTime.now(DateTimeZone.UTC)
      val weekDay = DayOfWeek.of(dateTime.getDayOfWeek)
      val hour = dateTime.getHourOfDay
      if (weekDay != DayOfWeek.SATURDAY && weekDay != DayOfWeek.SUNDAY
        && (hour >= 13 || (hour >= 12 && dateTime.getMinuteOfHour >= 30))
        && hour < 20) {
        requestStocks()
      }
  }

  private def requestStocks(): Unit = {
    service.allFavorites() match {
      case Right(favSeq) => favSeq.foreach(fav => service.addCurrentPriceRecord(fav))
      case Left(error) => Logger(error)
    }
  }
}
