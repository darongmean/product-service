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
    val Right(view) = Product.view(productId.toString)

    assert(view.productId == productId)
    assert(view.increment == 1)
  }

  test("When get most viewed products, default top 5 products") {
    val Right(mostView) = Product.mostView(null)
    assert(mostView.limit == 5)

    val Right(mostView2) = Product.mostView("-1")
    assert(mostView2.limit == 5)

    val Right(mostView3) = Product.mostView("0")
    assert(mostView3.limit == 5)
  }

  test("When get most viewed products, only include at least 1 view") {
    val Right(mostView) = Product.mostView(null)
    assert(mostView.minViewCount == 1)
  }
}
