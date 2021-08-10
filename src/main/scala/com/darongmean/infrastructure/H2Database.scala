package com.darongmean.infrastructure

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.H2Profile.api._

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class H2Database {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  val pooledDataSource = new ComboPooledDataSource
  val db = Database.forDataSource(pooledDataSource, None)

  def closeDbConnection(): Unit = {
    pooledDataSource.close()
  }

  def runAndWait[R](action: DBIO[R]): Either[Throwable, R] = {
    try {
      val result = db.run(action.transactionally)
      Right(Await.result(result, Duration(10, TimeUnit.SECONDS)))
    } catch {
      case ex: Throwable =>
        logger.error("db exception", ex)
        Left(ex)
    }
  }
}
