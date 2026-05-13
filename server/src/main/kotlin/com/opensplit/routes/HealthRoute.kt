package com.opensplit.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import com.opensplit.core.ServerMessages

fun Application.healthRoute() {
    routing {
        get("/health") {
            call.respondText(ServerMessages.healthResponse(), status = HttpStatusCode.OK)
        }
    }
}
