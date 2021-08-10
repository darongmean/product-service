package com.darongmean

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

object Product {
  private implicit lazy val jsonFormats: Formats = DefaultFormats.withBigDecimal + new BigDecimalSerializer()
  private val logger = LoggerFactory.getLogger(getClass)

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
                                      sortViewCount: String = "desc",
                                      convertCurrency: Option[String] = None)

  def create(requestBody: String): Either[String, InsertProduct] = try {
    val request = parse(requestBody).extract[InsertProduct]
    if (isNullOrEmpty(request.productName)) {
      return Left("productName is required")
    }

    if (null == request.productPriceUsd) {
      return Left("productPriceUsd is required")
    }

    Right(request)
  } catch {
    case ex: MappingException =>
      logger.info(requestBody + " " + ex.getCause.getMessage)
      Left("body is invalid")
    case ex: Throwable =>
      logger.error(requestBody, ex)
      Left("body is invalid")
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
      return Left("productId is invalid")
    }

    val paramCurrency = params.get("currency").map(_.toUpperCase)
    val currency = paramCurrency.filter(Set("USD", "CAD", "EUR", "GBP").contains)
    if (paramCurrency.isDefined && currency.isEmpty) {
      return Left("currency should be one of USD, CAD, EUR, GBP")
    }

    Try {
      paramProductId.toLong
    } match {
      case Success(v) => Right(UpdateViewCount(productId = v, convertCurrency = currency))
      case Failure(_) => Left("productId is invalid")
    }
  }

  def mostView(params: Map[String, String]): Either[String, SelectProductByViewCount] = {
    val paramLimit = params.get("limit")
    val parsedLimit = Try {
      paramLimit.get.toLong
    }
    val limit: Long = parsedLimit.getOrElse(5)
    if (paramLimit.isDefined && parsedLimit.isFailure) {
      return Left("limit should be an integer")
    }

    val paramCurrency = params.get("currency").map(_.toUpperCase)
    val currency = paramCurrency.filter(Set("USD", "CAD", "EUR", "GBP").contains)
    if (paramCurrency.isDefined && currency.isEmpty) {
      return Left("currency should be one of USD, CAD, EUR, GBP")
    }

    Right(SelectProductByViewCount(
      limit = if (limit <= 0) 5 else limit,
      convertCurrency = currency))
  }

  private def isNullOrEmpty(s: String) = null == s || s.isEmpty || s.isBlank
}

class BigDecimalSerializer extends CustomSerializer[BigDecimal](_ => ( {
  case JDecimal(x) => x
  case JDouble(x) => BigDecimal(x)
  case JInt(x) => BigDecimal(x)
  case JLong(x) => BigDecimal(x)
  case JNull => null
  case JString(x) => BigDecimal(x)
}, {
  case x: String => JString(x)
}))
