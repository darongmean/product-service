package com.darongmean.infrastructure

import org.scalatra._

/*
 - create a new product
   - name
   - price, in USD
   - description, optional
 - get a single product
   - all fields
   - increment view count
   - param currency => price convert to currenty
   - currency support: USD, CAD, EUR, GBP
   - exchange rate from https://currencylayer.com
 - list the most viewed product
   - default top 5
   - param limit => amount of products to be returned
   - only include at least 1 view
   - param currency
 - delete a product
   - not included in any api response
   - should remain in db for audit purpose
 */

class ProductHttpEndpoint extends ScalatraServlet {

  get("/") {
    <h1>Hello, World!</h1>
  }

}
