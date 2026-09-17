package com.opensplit.plugins

import com.opensplit.config.AppConfig
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import org.koin.ktor.ext.inject

fun Application.configureHTTP(isTest: Boolean = false) {
  val appConfig by inject<AppConfig>()

  install(CORS) {
    allowMethod(HttpMethod.Options)
    allowMethod(HttpMethod.Get)
    allowMethod(HttpMethod.Post)
    allowMethod(HttpMethod.Put)
    allowMethod(HttpMethod.Delete)
    allowMethod(HttpMethod.Patch)

    allowHeader(HttpHeaders.Authorization)
    allowHeader(HttpHeaders.ContentType)
    allowHeader(HttpHeaders.AccessControlAllowOrigin)

    allowHost("localhost:8081", schemes = listOf("http"))
    allowHost("127.0.0.1:8081", schemes = listOf("http"))
    allowHost("0.0.0.0:8081", schemes = listOf("http"))
  }

  intercept(ApplicationCallPipeline.Plugins) {
    if (appConfig.isDevelopment && !isTest) {
      delay(1.seconds)
    }
  }
}
