package com.darongmean.productservice

import org.scalatra.test.scalatest._

class ProductServiceServletTests extends ScalatraFunSuite {

  addServlet(classOf[ProductServiceServlet], "/*")

  test("GET / on ProductServiceServlet should return status 200") {
    get("/") {
      status should equal (200)
    }
  }

}
