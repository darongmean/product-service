package com.darongmean.workflow

import com.darongmean.Product
import com.darongmean.Product._
import com.darongmean.infrastructure.{CurrencyLayer, H2Database}
import slick.jdbc.H2Profile.api._


class GetProduct(db: H2Database, currencyLayer: CurrencyLayer) {

  def processRequest(params: Map[String, String]) = {
    for {
      view <- Product.view(params)
      productData <- selectProduct(view)
      _ <- incrementViewCount(view)
      productWithCurrencyConverted <- convertCurrency(view, productData)
    } yield {
      productWithCurrencyConverted
    }
  }

  private def selectProduct(view: UpdateViewCount) = {
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

  private def incrementViewCount(view: UpdateViewCount): Either[Throwable, Int] = {
    val statement =
      sqlu"""update ProductActive
             set viewCount = viewCount + ${view.increment}
             where productId = ${view.productId}"""
    db.runAndWait(statement)
  }

  private def convertCurrency(view: UpdateViewCount, data: ProductData) = try {
    view.convertCurrency match {
      case Some(currency) => Right(data.copy(
        priceCurrency = currency,
        productPrice = data.productPrice * currencyLayer.getExchangeRateFromUsd(currency)))
      case None => Right(data)
    }
  } catch {
    case ex: Throwable => Left(ex)
  }
}
