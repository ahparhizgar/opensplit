package com.opensplit.features.sync

import com.opensplit.features.auth.UserPrincipal
import com.opensplit.plugins.authenticateUser
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.syncModule() {
  routing {
    authenticateUser {
      get("/sync") {
        val syncService by inject<SyncService>()
        val user = call.principal<UserPrincipal>()!!
        val sinceVersion = call.request.queryParameters["sinceVersion"]?.toLongOrNull() ?: 0L

        val response = syncService.getChanges(sinceVersion, user)
        call.respond(response)
      }
    }
  }
}
