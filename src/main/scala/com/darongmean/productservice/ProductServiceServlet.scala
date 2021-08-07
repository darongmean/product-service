package com.darongmean.productservice

import org.scalatra._

class ProductServiceServlet extends ScalatraServlet {

  get("/") {
    <h1>Hello, World!</h1>
  }

}
