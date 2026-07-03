package com.opensplit

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {

  @Test
  fun healthEndpointReturnsOk() = testOpenSplit {
    val response = client.get("/health")
    assertEquals(HttpStatusCode.OK, response.status)
    assertEquals("ok", response.bodyAsText())
  }
}
