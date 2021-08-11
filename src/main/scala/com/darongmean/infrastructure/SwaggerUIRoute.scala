package com.darongmean.infrastructure

import org.scalatra._

class SwaggerUIRoute extends ScalatraServlet {
  get("/?") {
    redirect("/swagger-ui")
  }

  notFound {
    contentType = null
    serveStaticResource() getOrElse resourceNotFound()
  }
}
