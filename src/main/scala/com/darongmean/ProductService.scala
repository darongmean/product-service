package com.darongmean

import scala.util.{Failure, Success, Try}

object ProductService {

  case class ProductData(productId: Long,
                         productName: String,
                         productPriceUsd: BigDecimal,
                         productDescription: String)

  case class CreateProductRequest(productName: String = null,
                                  productPriceUsd: BigDecimal = null,
                                  productDescription: String = null)

  case class SingleProductResponse(status: Int,
                                   data: ProductData,
                                   detail: String = null,
                                   traceId: String = null)

  case class NoDataResponse(status: Int,
                            detail: String = null,
                            traceId: String = null)

  def validateCreateProductRequest(request: CreateProductRequest): Either[String, CreateProductRequest] = {
    if (isNullOrEmpty(request.productName)) {
      return Left("productName is required")
    }

    if (null == request.productPriceUsd) {
      return Left("productPriceUsd is required")
    }

    Right(request)
  }

  def validateProductId(paramProductId: String): Either[String, Long] = {
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

  private def isNullOrEmpty(s: String) = null == s || s.isEmpty || s.isBlank
}
