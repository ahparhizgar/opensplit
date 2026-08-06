package com.opensplit.plugins

import com.opensplit.config.AppConfig
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import org.koin.ktor.ext.inject

fun Application.configureHTTP(isTest: Boolean = false) {
  val appConfig by inject<AppConfig>()

  intercept(ApplicationCallPipeline.Plugins) {
    if (appConfig.isDevelopment && !isTest) {
      delay(1.seconds)
    }
  }
}
