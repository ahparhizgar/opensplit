package com.opensplit.features.auth

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.timeIntervalSince1970

@OptIn(BetaInteropApi::class)
actual fun platformDecodeBase64(input: String): String {
  val nsData = NSData.create(input, 0.toULong()) ?: return ""
  return nsData.toString()
}

actual fun currentTimeSeconds(): Long =
    (platform.Foundation.NSDate().timeIntervalSince1970).toLong()
