package com.opensplit.features.auth

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

fun isJwtExpired(token: String): Boolean {
    return try {
        val parts = token.split(".")
        if (parts.size != 3) return true
        val json = decodeJwtPayload(parts[1]) ?: return true
        val exp = json["exp"]?.jsonPrimitive?.longOrNull ?: return true
        currentTimeSeconds() >= exp
    } catch (_: Exception) {
        true
    }
}

private fun decodeJwtPayload(payload: String): JsonObject? {
    val base64 = payload.replace('-', '+').replace('_', '/')
    val padding = when (base64.length % 4) {
        2 -> "=="
        3 -> "="
        else -> ""
    }
    val decoded = platformDecodeBase64(base64 + padding)
    return Json.decodeFromString(decoded)
}

internal expect fun platformDecodeBase64(input: String): String

internal expect fun currentTimeSeconds(): Long
