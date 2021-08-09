package com.darongmean.workflow

import com.darongmean.ProductService
import com.darongmean.ProductService._
import com.darongmean.infrastructure.{H2Database, TraceId}
import org.scalatra._
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext

class CreateProduct(val db: H2Database) {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  implicit val ec: scala.concurrent.ExecutionContext = ExecutionContext.global

  def processRequest(request: CreateProductRequest): ActionResult = {
    val traceId = TraceId.get()

    val result = for {
      request <- ProductService.validateCreateProductRequest(request)
      productPk <- insertProduct(traceId, request)
      _ <- insertProductActive(productPk)
      productData <- selectProduct(productPk)
    } yield {
      productData
    }

    result match {
      case Right(productData) => Ok(CreateProductResponse(status = 200, data = productData, traceId = traceId))
      case Left(err: String) => BadRequest(NoDataResponse(status = 400, detail = err, traceId = traceId))
      case _ => InternalServerError(NoDataResponse(status = 500, traceId = traceId))
    }
  }

  def insertProduct(traceId: String, data: CreateProductRequest): Either[Throwable, Long] = {
    val statement =
      sqlu"""insert into Product(productName, productPriceUsd, productDescription, traceId)
             values (${data.productName},
                     ${data.productPriceUsd},
                     ${data.productDescription},
                     $traceId)""" andThen
        sql"""call identity()""".as[Long]
    db.runAndWait(statement) match {
      case Right(v: Vector[Long]) => Right(v.head)
      case Left(ex) => Left(ex)
    }
  }

  def insertProductActive(productPk: Long): Either[Throwable, Int] = {
    val statement =
      sqlu"""insert into ProductActive(productPk, productId, traceId)
             select productPk, productId, traceId
             from Product
             where productPk = ${productPk}"""
    db.runAndWait(statement)
  }

  def selectProduct(productPk: Long): Either[Throwable, ProductData] = {
    val statement =
      sql"""select productId, productName, productPriceUsd, productDescription
            from Product
            where productPk = ${productPk}""".as[(Long, String, BigDecimal, String)]
    db.runAndWait(statement) match {
      case Right(Vector(Tuple4(productId, productName, productPriceUsd, productDescription))) =>
        Right(ProductData(productId, productName, productPriceUsd, productDescription))
      case Left(ex) => Left(ex)
    }
  }
}
