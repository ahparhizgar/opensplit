package com.opensplit.common

import io.ktor.server.application.ApplicationCall
import java.net.URLDecoder
import kotlin.text.Charsets.UTF_8

fun ApplicationCall.readAccessToken(): String? {
  val raw = request.headers["Authorization"]?.removePrefix("Bearer ")
  return raw?.let { URLDecoder.decode(it, UTF_8.name()) }
}
