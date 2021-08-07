import com.darongmean.productservice.ProductServiceServlet
import org.scalatra._

import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new ProductServiceServlet, "/*")
  }
}
