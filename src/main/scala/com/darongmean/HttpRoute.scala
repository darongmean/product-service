package com.darongmean

import com.darongmean.Product._
import com.darongmean.infrastructure.{CurrencyLayer, H2Database, TraceId}
import com.darongmean.workflow._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json._

/*
- create a new product
   - name
   - price, in USD
   - description, optional
 - get a single product
   - all fields
   - increment view count
   - param currency => price convert to currenty
   - currency support: USD, CAD, EUR, GBP
   - exchange rate from https://currencylayer.com
 - list the most viewed product
   - default top 5
   - param limit => amount of products to be returned
   - only include at least 1 view
   - param currency
 - delete a product
   - not included in any api response
   - should remain in db for audit purpose
 */

class HttpRoute(val db: H2Database, currencyLayer: CurrencyLayer) extends ScalatraServlet with JacksonJsonSupport {
  protected implicit lazy val jsonFormats: Formats = DefaultFormats.withBigDecimal

  val createProduct = new CreateProduct(db)
  val deleteProduct = new DeleteProduct(db)
  val getProduct = new GetProduct(db, currencyLayer)
  val listMostViewProduct = new ListMostViewProduct(db, currencyLayer)

  before() {
    contentType = formats("json")
  }

  get("/") {
    Ok(s"{'status': 200, data='Hello World!', 'traceId': ${TraceId.get()}}")
  }

  post("/v1/product") {
    val traceId = TraceId.get()
    createProduct.processRequest(request.body) match {
      case Right(productData) => Ok(SingleProductResponse(status = 200, data = productData, traceId = traceId))
      case Left(err: String) => BadRequest(NoDataResponse(status = 400, detail = err, traceId = traceId))
      case _ => InternalServerError(NoDataResponse(status = 500, traceId = traceId))
    }
  }

  delete("/v1/product/:productId") {
    val traceId = TraceId.get()
    deleteProduct.processRequest(params("productId")) match {
      case Right(_) => Ok(NoDataResponse(status = 200, traceId = traceId))
      case Left(_: String) => Ok(NoDataResponse(status = 200, traceId = traceId))
      case _ => InternalServerError(NoDataResponse(status = 500, traceId = traceId))
    }
  }

  get("/v1/product/:productId") {
    val traceId = TraceId.get()
    getProduct.processRequest(params.toMap) match {
      case Right(productData) => Ok(SingleProductResponse(status = 200, data = productData, traceId = traceId))
      case Left(err: String) => BadRequest(NoDataResponse(status = 400, detail = err, traceId = traceId))
      case Left(null) => NotFound(NoDataResponse(status = 404, detail = "product not found", traceId = traceId))
      case _ => InternalServerError(NoDataResponse(status = 500, traceId = traceId))
    }
  }

  get("/v1/mostViewed") {
    val traceId = TraceId.get()
    listMostViewProduct.processRequest(params.toMap) match {
      case Right(productDataList) => Ok(MultiProductResponse(status = 200, data = productDataList, traceId = traceId))
      case Left(_: String) => Ok(NoDataResponse(status = 200, traceId = traceId))
      case _ => InternalServerError(NoDataResponse(status = 500, traceId = traceId))
    }
  }
}
