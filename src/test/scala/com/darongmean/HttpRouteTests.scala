package com.darongmean

import com.darongmean.Product._
import com.darongmean.infrastructure.{CurrencyLayer, H2Database}
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

  addServlet(new HttpRoute(db, new CurrencyLayer(null)), "/*")

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
      assert(parsedResponse.data.productPrice == someProductPrice)
      assert(parsedResponse.data.productDescription == someProductDescription)
      assert(parsedResponse.data.productId != 0)

      val selectRows = db.runAndWait(
        sql"select productPriceUsd from Product".as[BigDecimal]
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
    }
  }

  test("When a product is deleted, it's not included in any api response") {
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
    // delete product
    delete(s"/v1/product/$productId") {
      assert(status == 200)
    }
    // assert
    get(s"/v1/mostViewed?limit=10") {
      assert(status == 200)

      val parsedResponse = parse(body).extract[MultiProductResponse]
      assert(!parsedResponse.data.exists(p => p.productName == someProductName + "02"))
    }

    get(s"/v1/product/$productId") {
      assert(status == 404)
    }
  }

  test("When a product is deleted, it should remain in db for audit") {
    var productId: Long = 0
    // setup product
    post("/v1/product", write(InsertProduct(someProductName + "01", someProductPrice, someProductDescription))) {
      assert(status == 200)

      val parsedResponse = parse(body).extract[SingleProductResponse]
      productId = parsedResponse.data.productId
    }
    // delete product
    delete(s"/v1/product/$productId") {
      assert(status == 200)
    }
    // assert
    val selectProductRows = db.runAndWait(
      sql"select count(*) from Product where productId = $productId".as[Long]
    )
    assert(selectProductRows == Right(Vector(2)))
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
      assert(parsedResponse.data.productPrice == someProductPrice)
      assert(parsedResponse.data.productDescription == someProductDescription)
      assert(parsedResponse.data.productId == productId)

      val selectProductActiveRows = db.runAndWait(
        sql"select viewCount from ProductActive where productId = $productId".as[Long]
      )
      assert(selectProductActiveRows == Right(Vector(1)))
    }
  }

  test("Get /v1/product/productId should support param currency") {
    var productId: Long = 0

    post("/v1/product", write(InsertProduct(someProductName, someProductPrice, someProductDescription))) {
      assert(status == 200)

      val parsedResponse = parse(body).extract[SingleProductResponse]
      productId = parsedResponse.data.productId
    }

    get(s"/v1/product/$productId?currency=cad") {
      assert(status == 200)
      assert(header("Content-Type") == "application/json;charset=utf-8")

      val parsedResponse = parse(body).extract[SingleProductResponse]
      assert(parsedResponse.status == 200)
      assert(parsedResponse.data.priceCurrency == "CAD")
      assert(parsedResponse.data.productPrice != someProductPrice)
    }
  }

  test("Get /v1/mostViewed should return status 200") {
    var productId: Long = 0
    // setup product
    post("/v1/product", write(InsertProduct(someProductName + "01", someProductPrice, someProductDescription))) {
      assert(status == 200)

      val parsedResponse = parse(body).extract[SingleProductResponse]
      productId = parsedResponse.data.productId
    }
    get(s"/v1/product/$productId") {
      assert(status == 200)
    }
    // assert
    get(s"/v1/mostViewed?limit=10") {
      assert(status == 200)

      val parsedResponse = parse(body).extract[MultiProductResponse]
      assert(parsedResponse.data.length == 1)
      assert(parsedResponse.data.exists(p => p.productName == someProductName + "01"))
    }
  }

  test("Get /v1/mostViewed should return product at least 1 view") {
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
    get(s"/v1/mostViewed?limit=10") {
      assert(status == 200)

      val parsedResponse = parse(body).extract[MultiProductResponse]
      assert(parsedResponse.data.length == 2)
      assert(!parsedResponse.data.exists(p => p.productName == someProductName + "03"))
    }
  }

  test("Get /v1/mostViewed should accept currency parameter") {
    var productId: Long = 0
    // setup product
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
    // assert
    get(s"/v1/mostViewed?limit=10&currency=eur") {
      assert(status == 200)

      val parsedResponse = parse(body).extract[MultiProductResponse]
      assert(parsedResponse.data.map(_.priceCurrency) == Vector("EUR", "EUR"))
      assert(parsedResponse.data.forall(p => p.productPrice != someProductPrice))
    }
  }
}
