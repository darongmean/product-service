package com.darongmean.workflow

import com.darongmean.ProductService
import com.darongmean.ProductService.ProductData
import com.darongmean.infrastructure.H2Database
import slick.jdbc.H2Profile.api._


class GetProduct(db: H2Database) {

  def processRequest(paramProductId: String) = {
    for {
      productId <- ProductService.validateProductId(paramProductId)
      productData <- selectProduct(productId)
      _ <- incrementViewCount(productId)
    } yield {
      productData
    }
  }

  def selectProduct(productId: Long): Either[Throwable, ProductData] = {
    val statement =
      sql"""select p.productId, productName, productPriceUsd, productDescription
            from Product p
            join ProductActive pa on p.productPk = pa.productPk
            where pa.productId = ${productId}""".as[(Long, String, BigDecimal, String)]
    db.runAndWait(statement) match {
      case Right(Vector(Tuple4(productId, productName, productPriceUsd, productDescription))) =>
        Right(ProductData(productId, productName, productPriceUsd, productDescription))
      case Left(ex) => Left(ex)
    }
  }

  def incrementViewCount(productId: Long): Either[Throwable, Int] = {
    val statement =
      sqlu"""update ProductActive
             set viewCount = viewCount + 1
             where productId = ${productId}"""
    db.runAndWait(statement)
  }

}
