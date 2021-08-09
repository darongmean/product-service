package com.darongmean.workflow

import com.darongmean.ProductService
import com.darongmean.ProductService.ProductData
import com.darongmean.infrastructure.H2Database
import slick.jdbc.H2Profile.api._

class ListMostViewProduct(db: H2Database) {
  def processRequest(paramLimit: String) = {
    for {
      limit <- ProductService.validateLimit(paramLimit)
      productDataList <- selectProduct(limit)
    } yield {
      productDataList
    }
  }

  def selectProduct(limit: Long): Either[Throwable, Vector[ProductData]] = {
    val statement =
      sql"""select p.productId, productName, productPriceUsd, productDescription
            from Product p
            join ProductActive pa on p.productPk = pa.productPk
            where viewCount > 0
            order by viewCount desc
            limit ${limit}""".as[(Long, String, BigDecimal, String)]
    db.runAndWait(statement) match {
      case Right(products) =>
        Right(products map {
          case Tuple4(productId, productName, productPriceUsd, productDescription) => ProductData(productId, productName, productPriceUsd, productDescription)
        })
      case Left(ex) => Left(ex)
    }
  }

}
