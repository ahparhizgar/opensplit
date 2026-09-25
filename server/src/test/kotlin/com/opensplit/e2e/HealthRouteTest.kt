package com.opensplit.e2e

import com.opensplit.util.testOpenSplit
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals

class HealthRouteTest {

  @Test
  fun healthEndpointReturnsOk() = testOpenSplit {
    val response = client.get("/health")
    assertEquals(HttpStatusCode.OK, response.status)
    assertEquals("OK", response.bodyAsText())
  }
}
