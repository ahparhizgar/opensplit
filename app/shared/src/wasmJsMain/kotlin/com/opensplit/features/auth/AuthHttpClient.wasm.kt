package com.opensplit.features.auth

import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
internal actual fun platformDecodeBase64(input: String): String = js("atob(input)")

@OptIn(ExperimentalWasmJsInterop::class)
private fun jsCurrentTimeSeconds(): Double = js("Math.floor(Date.now() / 1000)")

internal actual fun currentTimeSeconds(): Long = jsCurrentTimeSeconds().toLong()
