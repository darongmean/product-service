package com.darongmean.infrastructure

import com.darongmean.ProductService.{CreateProductRequest, CreateProductResponse}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization._
import org.scalatest.BeforeAndAfterEach
import org.scalatra.test.scalatest._
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.H2Profile.api._


class ProductHttpEndpointTests extends ScalatraFunSuite with BeforeAndAfterEach {

  val logger: Logger = LoggerFactory.getLogger(getClass)
  val db = new H2Database

  implicit val jsonToObject: Formats = DefaultFormats.withBigDecimal

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    db.runAndWait(DBIO.seq(
      sqlu"truncate table Product",
      sqlu"truncate table ProductActive"))
  }

  protected override def afterAll(): Unit = {
    super.afterAll()
    db.closeDbConnection()
  }

  addServlet(new ProductHttpEndpoint(db), "/*")


  val someProductName = "name-abc-001"
  val someProductPrice: BigDecimal = 100.59
  val someProductDescription = "desc-xyz-987"

  test("POST /v1/product on ProductHttpEndpoint should return status 200") {
    post("/v1/product", write(CreateProductRequest(someProductName, someProductPrice, someProductDescription))) {
      assert(status == 200)
      assert(header("Content-Type") == "application/json;charset=utf-8")

      val parsedResponse = parse(body).extract[CreateProductResponse]
      assert(parsedResponse.status == 200)
      assert(parsedResponse.data.productName == someProductName)
      assert(parsedResponse.data.productPriceUsd == someProductPrice)
      assert(parsedResponse.data.productDescription == someProductDescription)
      assert(parsedResponse.data.productId != 0)
      val selectRows = db.runAndWait(
        sql"select productPriceUsd from Product".as[(BigDecimal)]
      )
      assert(selectRows == Right(Vector(someProductPrice)))
    }
  }
}
