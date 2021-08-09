package com.darongmean.workflow

import com.darongmean.Product
import com.darongmean.Product._
import com.darongmean.infrastructure.H2Database
import slick.jdbc.H2Profile.api._


class GetProduct(db: H2Database) {

  def processRequest(paramProductId: String) = {
    for {
      view <- Product.view(paramProductId)
      productData <- selectProduct(view)
      _ <- incrementViewCount(view)
    } yield {
      productData
    }
  }

  def selectProduct(view: UpdateViewCount) = {
    val statement =
      sql"""select p.productId, productName, productPriceUsd, productDescription
            from Product p
            join ProductActive pa on p.productPk = pa.productPk
            where pa.productId = ${view.productId}""".as[(Long, String, BigDecimal, String)]
    db.runAndWait(statement) match {
      case Right(Vector(Tuple4(productId, productName, productPriceUsd, productDescription))) =>
        Right(ProductData(productId, productName, productPriceUsd, productDescription))
      case Right(_) => Left(null)
      case Left(ex) => Left(ex)
    }
  }

  def incrementViewCount(view: UpdateViewCount): Either[Throwable, Int] = {
    val statement =
      sqlu"""update ProductActive
             set viewCount = viewCount + ${view.increment}
             where productId = ${view.productId}"""
    db.runAndWait(statement)
  }

}
