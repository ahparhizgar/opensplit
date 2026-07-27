package com.opensplit.features.sync

import com.opensplit.plugins.authenticateUser
import com.opensplit.plugins.user
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.syncModule() {
  routing {
    authenticateUser {
      get("/sync") {
        val syncService by inject<SyncService>()
        val sinceVersion = call.request.queryParameters["sinceVersion"]?.toLongOrNull() ?: 0L

        val response = syncService.getChanges(sinceVersion, call.user())
        call.respond(response)
      }
    }
  }
}
