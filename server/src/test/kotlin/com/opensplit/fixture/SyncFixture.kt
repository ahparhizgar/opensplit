package com.opensplit.fixture

import com.opensplit.dto.sync.SyncResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse

suspend fun HttpClient.syncRequest(sinceVersion: Long = 0L): HttpResponse =
    get("/sync?sinceVersion=$sinceVersion")

suspend fun HttpClient.sync(sinceVersion: Long = 0L): SyncResponse =
    syncRequest(sinceVersion).body<SyncResponse>()
