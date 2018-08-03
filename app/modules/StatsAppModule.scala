package modules

import actors.StockPriceRequestActor
import clients.{DefaultStockExchangeClient, StockExchangeClient}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import com.google.inject.name.Names
import javax.inject.{Inject, Singleton}
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import util.{DefaultStockExchangeService, StockExchangeService}

@Singleton
class StatsAppModule @Inject() () extends Module {
//  def configure() = {
//    bind(classOf[StockExchangeService]).to(classOf[DefaultStockExchangeService])
//    bind(classOf[StockExchangeClient]).to(classOf[DefaultStockExchangeClient])
//  }

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind(classOf[StockExchangeService]).to(classOf[DefaultStockExchangeService]),
    bind(classOf[StockExchangeClient]).to(classOf[DefaultStockExchangeClient])
  )

}

@Singleton
class StockPriceRequestModule @Inject() () extends AbstractModule with AkkaGuiceSupport {
    def configure(): Unit = {
      bindActor[StockPriceRequestActor]("StockPriceRequestActor")
    }
}
