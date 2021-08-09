package com.darongmean.workflow

import com.darongmean.Product
import com.darongmean.infrastructure.{H2Database, TraceId}
import slick.jdbc.H2Profile.api._


class DeleteProduct(db: H2Database) {

  def processRequest(paramProductId: String) = {
    val traceId = TraceId.get()

    for {
      productId <- Product.delete(paramProductId)
      _ <- insertProduct(traceId, productId)
      _ <- deleteProductActive(productId)
    } yield {
      productId
    }
  }

  def insertProduct(traceId: String, productId: Long): Either[Throwable, Int] = {
    val statement =
      sqlu"""insert into Product(productId, productName, productPriceUsd, productDescription, traceId, softDeleteAt)
             select p.productId, productName, productPriceUsd, productDescription, $traceId, current_time()
             from Product p
             join ProductActive pa on p.productPk = pa.productPk
             where pa.productId = ${productId}"""
    db.runAndWait(statement)
  }

  def deleteProductActive(productId: Long): Either[Throwable, Int] = {
    val statement =
      sqlu"""delete from ProductActive
             where productId = ${productId}"""
    db.runAndWait(statement)
  }

}
