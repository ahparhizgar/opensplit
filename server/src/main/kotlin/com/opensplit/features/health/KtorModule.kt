package com.opensplit.features.health

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.healthModule() {
  routing { route("/health") { get { call.respondText("OK", status = HttpStatusCode.OK) } } }
}
