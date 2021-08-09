import com.darongmean.HttpRoute
import com.darongmean.infrastructure.H2Database
import com.nike.wingtips.servlet.RequestTracingFilter
import org.scalatra._
import org.slf4j.{Logger, LoggerFactory}

import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  val db = new H2Database

  override def init(context: ServletContext): Unit = {
    context.mount(classOf[RequestTracingFilter], "/*")
    context.mount(new HttpRoute(db), "/*")
  }

  override def destroy(context: ServletContext): Unit = {
    super.destroy(context)
    db.closeDbConnection()
  }
}
