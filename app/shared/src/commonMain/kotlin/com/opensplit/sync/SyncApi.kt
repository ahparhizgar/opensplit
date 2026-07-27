package com.opensplit.sync

import com.opensplit.dto.sync.SyncResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

interface SyncApi {
  suspend fun getChanges(sinceVersion: Long): SyncResponse
}

class KtorSyncApi(private val client: HttpClient) : SyncApi {
  override suspend fun getChanges(sinceVersion: Long): SyncResponse {
    return client.get("/sync") { parameter("sinceVersion", sinceVersion) }.body()
  }
}
