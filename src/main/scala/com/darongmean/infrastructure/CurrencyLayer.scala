package com.darongmean.infrastructure

import org.apache.http.HttpResponse
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentHashMap
import java.util.{Timer, TimerTask}
import scala.jdk.CollectionConverters._

case class SuccessResponse(quotes: Map[String, BigDecimal])

class CurrencyLayer(val apiAccessKey: String) {

  implicit val jsonToObject: Formats = DefaultFormats.withBigDecimal

  private val logger = LoggerFactory.getLogger(getClass)

  private val seconds = 1000

  private val config = RequestConfig.custom()
    .setConnectTimeout(5 * seconds)
    .setConnectionRequestTimeout(5 * seconds)
    .setSocketTimeout(5 * seconds)
    .build()

  private val httpClient = HttpClientBuilder
    .create()
    .setDefaultRequestConfig(config)
    .build()

  val cacheExchangeRate: ConcurrentHashMap[String, BigDecimal] = defaultExchangeRate

  val timer = new Timer()

  def getExchangeRateFromUsd(currency: String): BigDecimal = {
    cacheExchangeRate.get(s"USD$currency") match {
      case rate: BigDecimal => rate
      case _ =>
        logger.error(s"no exchange rate for $currency")
        throw new Exception(s"no exchange rate for $currency")
    }
  }

  def setExchangeRate(): Unit = {
    executeGetLiveQuotes.foreach(response => {
      cacheExchangeRate.putAll(response.quotes.asJava)
    })
  }

  def init(): Unit = {
    timer.scheduleAtFixedRate(
      new TimerTask {
        override def run(): Unit = {
          setExchangeRate()
        }
      },
      0,
      60 * 60 * seconds)
  }

  def destroy(): Unit = {
    timer.cancel()
    timer.purge()
  }

  def defaultExchangeRate: ConcurrentHashMap[String, BigDecimal] = {
    val m = new ConcurrentHashMap[String, BigDecimal]()
    m.put("USDUSD", 1)
    m.put("USDEUR", 0.850195)
    m.put("USDGBP", 0.720685)
    m.put("USDCAD", 1.25366)
    m
  }

  protected def executeGetLiveQuotes: Option[SuccessResponse] = {
    val url = s"http://apilayer.net/api/live?currencies=EUR,GBP,CAD&source=USD&format=1"
    try {
      val getMethod = new HttpGet(s"$url&access_key=$apiAccessKey")
      val response = httpClient.execute(getMethod)
      val statusCode = getStatusCode(response)
      val body = getBody(response)

      logger.info(s"Get $url $statusCode $body")

      Some(parseResponse(body))
    } catch {
      case ex: Throwable =>
        logger.error(s"Get $url", ex)
        None
    }
  }

  protected def parseResponse(body: String): SuccessResponse = {
    parse(body).extract[SuccessResponse]
  }

  private def getStatusCode(response: HttpResponse) = {
    response.getStatusLine.getStatusCode
  }

  private def getBody(response: HttpResponse) = {
    EntityUtils.toString(response.getEntity)
  }
}
