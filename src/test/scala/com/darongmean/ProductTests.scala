package com.darongmean

import com.darongmean.Product._
import org.scalatest.funsuite.AnyFunSuite

class ProductTests extends AnyFunSuite {

  test("When create product, product name is required") {
    assert(Product.create(InsertProduct(productName = null)) == Left("productName is required"))
    assert(Product.create(InsertProduct(productName = "")) == Left("productName is required"))
    assert(Product.create(InsertProduct(productName = "   ")) == Left("productName is required"))
  }

  test("When create product, product price is required") {
    val productNoPrice = InsertProduct(productName = "a", productPriceUsd = null)

    assert(Product.create(productNoPrice) == Left("productPriceUsd is required"))
  }

  test("When create product, product description is optional") {
    val product = InsertProduct(productName = "a", productPriceUsd = 0)

    assert(Product.create(product) == Right(product))
  }

  test("When get a single product, increment view count by 1") {
    val productId = 123
    val Right(view) = Product.view(Map("productId" -> productId.toString))

    assert(view.productId == productId)
    assert(view.increment == 1)
  }

  test("When get a single product, convert currency to USD is supported") {
    val Right(view) = Product.view(
      Map("productId" -> "123", "currency" -> "usd"))

    assert(view.convertCurrency.contains("USD"))
  }

  test("When get a single product, convert currency to CAD is supported") {
    val Right(view) = Product.view(
      Map("productId" -> "123", "currency" -> "cad"))

    assert(view.convertCurrency.contains("CAD"))
  }

  test("When get a single product, convert currency to GBP is supported") {
    val Right(view) = Product.view(
      Map("productId" -> "123", "currency" -> "gbp"))

    assert(view.convertCurrency.contains("GBP"))
  }

  test("When get a single product, convert currency to EUR is supported") {
    val Right(view) = Product.view(
      Map("productId" -> "123", "currency" -> "eur"))

    assert(view.convertCurrency.contains("EUR"))
  }

  test("When get most viewed products, default top 5 products") {
    val Right(mostView) = Product.mostView(Map())
    assert(mostView.limit == 5)

    val Right(mostView2) = Product.mostView(Map("limit" -> "-1"))
    assert(mostView2.limit == 5)

    val Right(mostView3) = Product.mostView(Map("limit" -> "0"))
    assert(mostView3.limit == 5)

    val mostView4 = Product.mostView(Map("limit" -> "abc"))
    assert(mostView4 == Left("limit should be an integer"))
  }

  test("When get most viewed products, only include at least 1 view") {
    val Right(mostView) = Product.mostView(Map())
    assert(mostView.minViewCount == 1)
  }

  test("When get most viewed products, convert currency to USD is supported") {
    val Right(mostView) = Product.mostView(Map("currency" -> "usd"))
    assert(mostView.convertCurrency.contains("USD"))
  }

  test("When get most viewed products, convert currency to GBP is supported") {
    val Right(mostView) = Product.mostView(Map("currency" -> "gbp"))
    assert(mostView.convertCurrency.contains("GBP"))
  }

  test("When get most viewed products, convert currency to EUR is supported") {
    val Right(mostView) = Product.mostView(Map("currency" -> "eur"))
    assert(mostView.convertCurrency.contains("EUR"))
  }

  test("When get most viewed products, convert currency to CAD is supported") {
    val Right(mostView) = Product.mostView(Map("currency" -> "cad"))
    assert(mostView.convertCurrency.contains("CAD"))
  }
}
