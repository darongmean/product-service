package com.darongmean.workflow

import com.darongmean.Product
import com.darongmean.Product._
import com.darongmean.infrastructure.{H2Database, TraceId}
import slick.jdbc.H2Profile.api._

class CreateProduct(val db: H2Database) {

  def processRequest(requestBody: String) = {
    val traceId = TraceId.get()

    for {
      data <- Product.create(requestBody)
      productPk <- insertProduct(traceId, data)
      _ <- insertProductActive(productPk)
      productData <- selectProduct(productPk)
    } yield {
      productData
    }
  }

  def insertProduct(traceId: String, data: InsertProduct): Either[Throwable, Long] = {
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
