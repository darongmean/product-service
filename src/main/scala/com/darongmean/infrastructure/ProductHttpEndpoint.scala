package com.darongmean.infrastructure

import com.darongmean.ProductService._
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

class ProductHttpEndpoint(val db: H2Database) extends ScalatraServlet with JacksonJsonSupport {
  protected implicit lazy val jsonFormats: Formats = DefaultFormats.withBigDecimal

  val createProduct = new CreateProduct(db)
  val deleteProduct = new DeleteProduct(db)
  val getProduct = new GetProduct(db)
  val listMostViewProduct = new ListMostViewProduct(db)

  before() {
    contentType = formats("json")
  }

  get("/") {
    Ok(s"{'status': 200, data='Hello World!', 'traceId': ${TraceId.get()}}")
  }

  post("/v1/product") {
    val traceId = TraceId.get()
    createProduct.processRequest(parsedBody.extract[CreateProductRequest]) match {
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
    getProduct.processRequest(params("productId")) match {
      case Right(productData) => Ok(SingleProductResponse(status = 200, data = productData, traceId = traceId))
      case Left(err: String) => BadRequest(NoDataResponse(status = 400, detail = err, traceId = traceId))
      case _ => InternalServerError(NoDataResponse(status = 500, traceId = traceId))
    }
  }

  get("/v1/product/mostView") {
    val traceId = TraceId.get()
    listMostViewProduct.processRequest(params("limit")) match {
      case Right(productDataList) => Ok(MultiProductResponse(status = 200, data = productDataList, traceId = traceId))
      case Left(_: String) => Ok(NoDataResponse(status = 200, traceId = traceId))
      case _ => InternalServerError(NoDataResponse(status = 500, traceId = traceId))
    }
  }
}
