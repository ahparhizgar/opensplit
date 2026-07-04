package com.opensplit.features.auth

actual fun platformDecodeBase64(input: String): String =
    java.util.Base64.getDecoder().decode(input).decodeToString()

actual fun currentTimeSeconds(): Long = System.currentTimeMillis() / 1000
