import com.darongmean.infrastructure.ProductHttpEndpoint
import com.nike.wingtips.servlet.RequestTracingFilter
import org.scalatra._

import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(classOf[RequestTracingFilter], "/*")
    context.mount(classOf[ProductHttpEndpoint], "/*")
  }
}
