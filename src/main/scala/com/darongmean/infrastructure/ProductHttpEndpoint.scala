package com.darongmean.infrastructure

import com.darongmean.ProductService.CreateProductRequest
import com.darongmean.workflow.{CreateProduct, DeleteProduct}
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

  before() {
    contentType = formats("json")
  }

  get("/") {
    Ok(s"{'status': 200, data='Hello World!', 'traceId': ${TraceId.get()}}")
  }

  post("/v1/product") {
    createProduct.processRequest(parsedBody.extract[CreateProductRequest])
  }

  delete("/v1/product/:productId") {
    deleteProduct.processRequest(params("productId"))
  }
}
