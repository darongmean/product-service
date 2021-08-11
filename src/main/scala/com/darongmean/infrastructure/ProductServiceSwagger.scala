package com.darongmean.infrastructure

import org.scalatra.ScalatraServlet
import org.scalatra.swagger._

class SwaggerRoute(implicit val swagger: Swagger) extends ScalatraServlet with JacksonSwaggerBase

object ProductServiceApiInfo extends ApiInfo(
  "The Product Service API",
  "Docs for the Product Service API",
  "",
  ContactInfo(
    "Darong Mean",
    "https://darongmean.com",
    ""
  ),
  LicenseInfo(
    "MIT",
    "http://opensource.org/licenses/MIT"
  ))

class ProductServiceSwagger extends Swagger(Swagger.SpecVersion, "1.0.0", ProductServiceApiInfo) {

}
