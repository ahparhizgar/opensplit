package com.opensplit.features.auth

import android.util.Base64

actual fun platformDecodeBase64(input: String): String =
    Base64.decode(input, Base64.DEFAULT).decodeToString()

actual fun currentTimeSeconds(): Long = System.currentTimeMillis() / 1000
