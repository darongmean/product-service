import com.darongmean.HttpRoute
import com.darongmean.infrastructure._
import com.nike.wingtips.servlet.RequestTracingFilter
import org.scalatra._
import org.slf4j.{Logger, LoggerFactory}

import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  implicit val swagger: ProductServiceSwagger = new ProductServiceSwagger

  val db = new H2Database
  val currencyLayerApiAccessKey: Option[String] = sys.env.get("CURRENCY_LAYER_API_ACCESS_KEY")
  var currencyLayer: CurrencyLayer = Option.empty[CurrencyLayer].orNull

  override def init(context: ServletContext): Unit = {
    if (currencyLayerApiAccessKey.forall(_.isBlank)) {
      logger.error("CURRENCY_LAYER_API_ACCESS_KEY environment variable must be set")
    }
    currencyLayer = new CurrencyLayer(currencyLayerApiAccessKey.get)
    currencyLayer.init()

    context.mount(classOf[RequestTracingFilter], "/*")
    context.mount(new HttpRoute(db, currencyLayer), "/v1", "v1")
    context.mount(new SwaggerUIRoute, "/*")
    context.mount(new SwaggerRoute, "/api-docs")
  }

  override def destroy(context: ServletContext): Unit = {
    super.destroy(context)
    db.closeDbConnection()
    if (null != currencyLayer) {
      currencyLayer.destroy()
    }
  }
}
