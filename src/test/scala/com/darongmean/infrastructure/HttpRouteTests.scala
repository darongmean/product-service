package com.darongmean.infrastructure

import com.darongmean.HttpRoute
import com.darongmean.Product._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization._
import org.scalatest.BeforeAndAfterEach
import org.scalatra.test.scalatest._
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.H2Profile.api._


class HttpRouteTests extends ScalatraFunSuite with BeforeAndAfterEach {

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

  addServlet(new HttpRoute(db), "/*")

  val someProductName = "name-abc-001"
  val someProductPrice: BigDecimal = 100.59
  val someProductDescription = "desc-xyz-987"

  test("POST /v1/product should return status 200") {
    post("/v1/product", write(InsertProduct(someProductName, someProductPrice, someProductDescription))) {
      assert(status == 200)
      assert(header("Content-Type") == "application/json;charset=utf-8")

      val parsedResponse = parse(body).extract[SingleProductResponse]
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

  test("DELETE /v1/product should return status 200") {
    var productId: Long = 0

    post("/v1/product", write(InsertProduct(someProductName, someProductPrice, someProductDescription))) {
      assert(status == 200)

      val parsedResponse = parse(body).extract[SingleProductResponse]
      productId = parsedResponse.data.productId
    }

    delete(s"/v1/product/$productId") {
      assert(status == 200)
      assert(header("Content-Type") == "application/json;charset=utf-8")

      val selectProductActiveRows = db.runAndWait(
        sql"select productId from ProductActive where productId = $productId".as[Long]
      )
      assert(selectProductActiveRows == Right(Vector()))

      val selectProductRows = db.runAndWait(
        sql"select count(*) from Product where productId = $productId".as[Long]
      )
      assert(selectProductRows == Right(Vector(2)))
    }
  }

  test("Get /v1/product should return status 200") {
    var productId: Long = 0

    post("/v1/product", write(InsertProduct(someProductName, someProductPrice, someProductDescription))) {
      assert(status == 200)

      val parsedResponse = parse(body).extract[SingleProductResponse]
      productId = parsedResponse.data.productId
    }

    get(s"/v1/product/$productId") {
      assert(status == 200)
      assert(header("Content-Type") == "application/json;charset=utf-8")

      val parsedResponse = parse(body).extract[SingleProductResponse]
      assert(parsedResponse.status == 200)
      assert(parsedResponse.data.productName == someProductName)
      assert(parsedResponse.data.productPriceUsd == someProductPrice)
      assert(parsedResponse.data.productDescription == someProductDescription)
      assert(parsedResponse.data.productId == productId)

      val selectProductActiveRows = db.runAndWait(
        sql"select viewCount from ProductActive where productId = $productId".as[Long]
      )
      assert(selectProductActiveRows == Right(Vector(1)))
    }
  }

  test("Get /v1/product/mostView should return status 200") {
    var productId: Long = 0
    // setup product 01
    post("/v1/product", write(InsertProduct(someProductName + "01", someProductPrice, someProductDescription))) {
      assert(status == 200)

      val parsedResponse = parse(body).extract[SingleProductResponse]
      productId = parsedResponse.data.productId
    }
    get(s"/v1/product/$productId") {
      assert(status == 200)
    }
    get(s"/v1/product/$productId") {
      assert(status == 200)
    }
    // setup product 02
    post("/v1/product", write(InsertProduct(someProductName + "02", someProductPrice, someProductDescription))) {
      assert(status == 200)

      val parsedResponse = parse(body).extract[SingleProductResponse]
      productId = parsedResponse.data.productId
    }
    get(s"/v1/product/$productId") {
      assert(status == 200)
    }
    // setup product 03
    post("/v1/product", write(InsertProduct(someProductName + "03", someProductPrice, someProductDescription))) {
      assert(status == 200)
    }
    // assert
    get(s"/v1/product/mostView?limit=10") {
      assert(status == 200)

      val parsedResponse = parse(body).extract[MultiProductResponse]
      assert(parsedResponse.data.length == 2)
      assert(parsedResponse.data.exists(p => p.productName == someProductName + "01"))
      assert(parsedResponse.data.exists(p => p.productName == someProductName + "02"))
      assert(!parsedResponse.data.exists(p => p.productName == someProductName + "03"))
    }
  }
}
