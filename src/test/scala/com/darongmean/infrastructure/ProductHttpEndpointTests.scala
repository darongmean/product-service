package com.darongmean.infrastructure

import org.scalatra.test.scalatest._

class ProductHttpEndpointTests extends ScalatraFunSuite {

  addServlet(classOf[ProductHttpEndpoint], "/*")

  test("GET / on ProductServiceServlet should return status 200") {
    get("/") {
      status should equal(200)
    }
  }

}
