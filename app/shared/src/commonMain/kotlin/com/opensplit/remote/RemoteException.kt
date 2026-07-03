package com.opensplit.remote

/**
 * Exception thrown when a remote API call fails. Carries structured field-level and general error
 * messages matching the server's [ErrorResponse].
 */
data class RemoteException(
    val fieldErrors: Map<String, String> = emptyMap(),
    val generalError: String? = null,
) : RuntimeException(generalError ?: "Request failed")
