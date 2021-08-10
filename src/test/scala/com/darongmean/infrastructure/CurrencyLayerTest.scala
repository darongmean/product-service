package com.darongmean.infrastructure

import org.scalatest.funsuite.AnyFunSuite

import scala.io.Source

class CurrencyLayerTests extends AnyFunSuite {
  test("When currencylayer respond with success, we could get exchange rate") {
    val currencyLayer = new GoodResponseCurrencyLayer
    currencyLayer.setExchangeRate()

    assert(currencyLayer.getExchangeRateFromUsd("USD") == 1)
  }

  test("When currencylayer respond empty, we could get exchange rate using default") {
    val currencyLayer = new ErrorCurrencyLayer
    currencyLayer.setExchangeRate()

    assert(currencyLayer.getExchangeRateFromUsd("GBP") == currencyLayer.defaultExchangeRate.get("USDGBP"))
  }
}

class GoodResponseCurrencyLayer extends CurrencyLayer(null) {
  override protected def executeGetLiveQuotes: Option[SuccessResponse] = {
    val sample = Source
      .fromResource("apilayer.net_api_live_currencies-EUR-GBP-CAD_source-USD_format-1.json")
      .mkString
    Some(parseResponse(sample))
  }
}

class ErrorCurrencyLayer extends CurrencyLayer(null) {
  override protected def executeGetLiveQuotes: Option[SuccessResponse] = {
    None
  }
}
