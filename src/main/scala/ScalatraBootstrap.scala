import com.darongmean.HttpRoute
import com.darongmean.infrastructure.{CurrencyLayer, H2Database}
import com.nike.wingtips.servlet.RequestTracingFilter
import org.scalatra._
import org.slf4j.{Logger, LoggerFactory}

import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  val db = new H2Database
  val currencyLayerApiAccessKey: Option[String] = sys.env.get("CURRENCY_LAYER_API_ACCESS_KEY")
  var currencyLayer: CurrencyLayer = null

  override def init(context: ServletContext): Unit = {
    currencyLayerApiAccessKey.filterNot(_.isBlank) match {
      case Some(v) => v
      case None => {
        logger.error("CURRENCY_LAYER_API_ACCESS_KEY environment variable must be set")
        throw new Exception("CURRENCY_LAYER_API_ACCESS_KEY environment variable must be set")
      }
    }
    currencyLayer = new CurrencyLayer(currencyLayerApiAccessKey.get)
    currencyLayer.init()

    context.mount(classOf[RequestTracingFilter], "/*")
    context.mount(new HttpRoute(db, currencyLayer), "/*")
  }

  override def destroy(context: ServletContext): Unit = {
    super.destroy(context)
    db.closeDbConnection()
    if (null != currencyLayer) {
      currencyLayer.destroy()
    }
  }
}
