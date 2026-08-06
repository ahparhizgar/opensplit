package com.opensplit.remote

import com.ahparhizgar.katch.ApiCallError
import com.ahparhizgar.katch.BadRequest
import com.ahparhizgar.katch.ClientError
import com.ahparhizgar.katch.Forbidden
import com.ahparhizgar.katch.HttpError
import com.ahparhizgar.katch.InvalidDataError
import com.ahparhizgar.katch.NetworkError
import com.ahparhizgar.katch.NotFound
import com.ahparhizgar.katch.RateLimitReached
import com.ahparhizgar.katch.ServerError
import com.ahparhizgar.katch.Unauthorized

@Suppress("UNCHECKED_CAST")
val ApiCallError.fieldErrors: Map<String, String>
  get() = payload as? Map<String, String> ?: emptyMap()

val ApiCallError.userMessage: String
  get() {
    val baseMessage =
        when (this) {
          is ClientError ->
              userMessage
                  ?: when (this) {
                    is BadRequest -> "Invalid request"
                    is Unauthorized -> "Unauthorized access"
                    is Forbidden -> "Access denied"
                    is NotFound -> "Content not found"
                    is RateLimitReached -> "Too many requests"
                    else -> "A client error occurred"
                  }
          is NetworkError -> "Internet is disconnected"
          is ServerError -> "An error occurred on our side"
          is InvalidDataError -> message ?: "An error occurred during processing of data"
          else -> message ?: "An unknown error occurred"
        }

    return if (this is HttpError) {
      val debugInfo = " ($code${if (!message.isNullOrBlank()) ": $message" else ""})"
      baseMessage + debugInfo
    } else {
      baseMessage
    }
  }
