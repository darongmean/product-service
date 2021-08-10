package com.darongmean.workflow

import com.darongmean.Product
import com.darongmean.Product.{ProductData, SelectProductByViewCount}
import com.darongmean.infrastructure.{CurrencyLayer, H2Database}
import slick.jdbc.H2Profile.api._

class ListMostViewProduct(db: H2Database, currencyLayer: CurrencyLayer) {

  def processRequest(params: Map[String, String]) = {
    for {
      criteria <- Product.mostView(params)
      productDataList <- selectProduct(criteria)
      productDataListWithCurrencyConverted <- convertCurrency(criteria, productDataList)
    } yield {
      productDataListWithCurrencyConverted
    }
  }

  def selectProduct(criteria: SelectProductByViewCount) = {
    val statement =
      sql"""select p.productId, productName, productPriceUsd, productDescription
            from Product p
            join ProductActive pa on p.productPk = pa.productPk
            where viewCount >= ${criteria.minViewCount}
            order by viewCount #${criteria.sortViewCount}
            limit ${criteria.limit}""".as[(Long, String, BigDecimal, String)]
    db.runAndWait(statement) match {
      case Right(products) =>
        Right(products map {
          case Tuple4(productId, productName, productPriceUsd, productDescription) => ProductData(productId, productName, productPriceUsd, productDescription)
        })
      case Left(ex) => Left(ex)
    }
  }

  private def convertCurrency(criteria: SelectProductByViewCount, productDataList: Vector[ProductData]) = try {
    criteria.convertCurrency match {
      case Some(currency) => Right(productDataList.map(data => data.copy(
        priceCurrency = currency,
        productPrice = data.productPrice * currencyLayer.getExchangeRateFromUsd(currency))))
      case None => Right(productDataList)
    }
  } catch {
    case ex: Throwable => Left(ex)
  }

}
