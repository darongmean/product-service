package com.darongmean.infrastructure

import com.nike.wingtips.Tracer

import java.util.UUID

object TraceId {
  def get(): String = {
    val span = Tracer.getInstance().getCurrentSpan
    if (span == null) UUID.randomUUID().toString else span.getTraceId
  }
}
