package com.opensplit.remote

import com.ahparhizgar.katch.ApiCallError
import com.ahparhizgar.katch.ClientError

val ApiCallError.fieldErrors: Map<String, String>
  get() = (this.payload as? ClientError)?.fieldErrors ?: emptyMap()

val ApiCallError.userMessage: String?
  get() = (this as? ClientError)?.userMessage
