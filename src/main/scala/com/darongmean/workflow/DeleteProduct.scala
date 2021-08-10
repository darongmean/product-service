package com.darongmean.workflow

import com.darongmean.Product
import com.darongmean.infrastructure.{H2Database, TraceId}
import slick.jdbc.H2Profile.api._


class DeleteProduct(db: H2Database) {

  def processRequest(paramProductId: String): Either[Serializable, Long] = {
    val traceId = TraceId.get()

    for {
      productId <- Product.delete(paramProductId)
      _ <- insertProduct(traceId, productId)
      _ <- deleteProductActive(productId)
    } yield {
      productId
    }
  }

  private def insertProduct(traceId: String, productId: Long) = {
    val statement =
      sqlu"""insert into Product(productId, productName, productPriceUsd, productDescription, traceId, softDeleteAt)
             select p.productId, productName, productPriceUsd, productDescription, $traceId, current_time()
             from Product p
             join ProductActive pa on p.productPk = pa.productPk
             where pa.productId = $productId"""
    db.runAndWait(statement)
  }

  private def deleteProductActive(productId: Long) = {
    val statement =
      sqlu"""delete from ProductActive
             where productId = $productId"""
    db.runAndWait(statement)
  }

}
