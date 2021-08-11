package com.darongmean

import com.darongmean.Product._
import com.darongmean.infrastructure.{CurrencyLayer, H2Database, TraceId}
import com.darongmean.workflow._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json._
import org.scalatra.swagger.{Swagger, SwaggerSupport}

class HttpRoute(val db: H2Database, currencyLayer: CurrencyLayer)(implicit val swagger: Swagger)
  extends ScalatraServlet with JacksonJsonSupport with SwaggerSupport {
  protected implicit lazy val jsonFormats: Formats = DefaultFormats.withBigDecimal

  val createProduct = new CreateProduct(db)
  val deleteProduct = new DeleteProduct(db)
  val getProduct = new GetProduct(db, currencyLayer)
  val listMostViewProduct = new ListMostViewProduct(db, currencyLayer)

  before() {
    contentType = formats("json")
  }

  post("/product",
    operation(apiOperation[SingleProductResponse]("createProduct")
      summary "Create a single product."
      parameters bodyParam[InsertProduct]("product").description(
      """The product information:
        |- productName, required, is the name of the product. Max 255 characters.
        |- productPriceUsd, required, is the price of the product in USD.
        |- productDescription, optional, is the description of the product. Max 5000 characters.
        |""".stripMargin))) {
    val traceId = TraceId.get()
    createProduct.processRequest(request.body) match {
      case Right(productData) => Ok(SingleProductResponse(status = 200, data = productData, traceId = traceId))
      case Left(err: String) => BadRequest(NoDataResponse(status = 400, detail = err, traceId = traceId))
      case _ => InternalServerError(NoDataResponse(status = 500, traceId = traceId))
    }
  }

  delete("/product/:productId",
    operation(apiOperation[NoDataResponse]("deleteProduct")
      summary "Delete a single product. However, it remains in the database for audit purposes."
      parameters pathParam[Long]("productId").description("The identifier of the product."))) {
    val traceId = TraceId.get()
    deleteProduct.processRequest(params("productId")) match {
      case Right(_) => Ok(NoDataResponse(status = 200, traceId = traceId))
      case Left(_: String) => Ok(NoDataResponse(status = 200, traceId = traceId))
      case _ => InternalServerError(NoDataResponse(status = 500, traceId = traceId))
    }
  }

  get("/product/:productId",
    operation(apiOperation[SingleProductResponse]("getProduct")
      summary "Return a single product information. And also increment the view-count for the product."
      parameters(
      pathParam[Long]("productId").description("The identifier of the product."),
      queryParam[Option[String]]("currency").description("The currency of the productPrice to be converted into. One of USD, CAD, EUR, GBP.")))) {
    val traceId = TraceId.get()
    getProduct.processRequest(params.toMap) match {
      case Right(productData) => Ok(SingleProductResponse(status = 200, data = productData, traceId = traceId))
      case Left(err: String) => BadRequest(NoDataResponse(status = 400, detail = err, traceId = traceId))
      case Left(null) => NotFound(NoDataResponse(status = 404, detail = "product not found", traceId = traceId))
      case _ => InternalServerError(NoDataResponse(status = 500, traceId = traceId))
    }
  }

  get("/mostViewed",
    operation(apiOperation[MultiProductResponse]("listMostViewProduct")
      summary "Return the products with the highest view-counts. Only products with at least 1 view is included."
      parameters(
      queryParam[Option[String]]("limit").description("The maximum number of products to be returned. Default 5."),
      queryParam[Option[String]]("currency").description("The currency of the productPrice to be converted into. One of USD, CAD, EUR, GBP.")))) {
    val traceId = TraceId.get()
    listMostViewProduct.processRequest(params.toMap) match {
      case Right(productDataList) => Ok(MultiProductResponse(status = 200, data = productDataList, traceId = traceId))
      case Left(_: String) => Ok(NoDataResponse(status = 200, traceId = traceId))
      case _ => InternalServerError(NoDataResponse(status = 500, traceId = traceId))
    }
  }

  override protected def applicationDescription: String = "The Product Service"
}
