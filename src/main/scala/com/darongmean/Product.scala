package com.darongmean

import scala.util.{Failure, Success, Try}

object Product {

  // http response
  case class ProductData(productId: Long,
                         productName: String,
                         productPrice: BigDecimal,
                         productDescription: String,
                         priceCurrency: String = "USD")

  case class NoDataResponse(status: Int,
                            detail: String = null,
                            traceId: String = null)

  case class SingleProductResponse(status: Int,
                                   data: ProductData,
                                   detail: String = null,
                                   traceId: String = null)

  case class MultiProductResponse(status: Int,
                                  data: Vector[ProductData],
                                  detail: String = null,
                                  traceId: String = null)

  // domain model
  case class InsertProduct(productName: String = null,
                           productPriceUsd: BigDecimal = null,
                           productDescription: String = null)

  case class UpdateViewCount(productId: Long,
                             increment: Long = 1,
                             convertCurrency: Option[String] = None)

  case class SelectProductByViewCount(limit: Long,
                                      minViewCount: Long = 1,
                                      sortViewCount: String = "desc")

  def create(request: InsertProduct): Either[String, InsertProduct] = {
    if (isNullOrEmpty(request.productName)) {
      return Left("productName is required")
    }

    if (null == request.productPriceUsd) {
      return Left("productPriceUsd is required")
    }

    Right(request)
  }

  def delete(paramProductId: String): Either[String, Long] = {
    if (isNullOrEmpty(paramProductId)) {
      return Left("productId is invalid")
    }

    Try {
      paramProductId.toLong
    } match {
      case Success(v) => Right(v)
      case Failure(_) => Left("productId is invalid")
    }
  }

  def view(params: Map[String, String]): Either[String, UpdateViewCount] = {
    val paramProductId = params.getOrElse("productId", null)
    if (isNullOrEmpty(paramProductId)) {
      return Left("param productId is invalid")
    }

    val paramCurrency = params.get("currency").map(_.toUpperCase)
    val currency = paramCurrency.filter(Set("USD", "CAD", "EUR", "GBP").contains)
    if (paramCurrency.isDefined && currency.isEmpty) {
      return Left("param currency should be one of USD, CAD, EUR, GBP")
    }

    Try {
      paramProductId.toLong
    } match {
      case Success(v) => Right(UpdateViewCount(productId = v, convertCurrency = currency))
      case Failure(_) => Left("param productId is invalid")
    }
  }

  def mostView(paramLimit: String): Either[String, SelectProductByViewCount] = {
    if (isNullOrEmpty(paramLimit)) {
      return Right(SelectProductByViewCount(limit = 5))
    }

    Try {
      paramLimit.toLong
    } match {
      case Success(v) => Right(SelectProductByViewCount(limit = if (v <= 0) 5 else v))
      case Failure(_) => Left("limit is invalid")
    }
  }

  private def isNullOrEmpty(s: String) = null == s || s.isEmpty || s.isBlank
}
