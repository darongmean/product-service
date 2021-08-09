package com.darongmean

object ProductService {

  case class ProductData(productId: Long,
                         productName: String,
                         productPriceUsd: BigDecimal,
                         productDescription: String)

  case class CreateProductRequest(productName: String = null,
                                  productPriceUsd: BigDecimal = null,
                                  productDescription: String = null)

  case class CreateProductResponse(status: Int,
                                   data: ProductData = null,
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

  private def isNullOrEmpty(s: String) = null == s || s.isEmpty || s.isBlank
}
