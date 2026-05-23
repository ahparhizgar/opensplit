package com.opensplit.features.auth

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.http.HttpHeaders
import io.ktor.util.AttributeKey

/**
 * A Ktor [HttpClientPlugin] that reads the stored access token from [TokenStorage] and
 * automatically appends an Authorization header to every outgoing request.
 *
 * Install this plugin when constructing an [HttpClient] that requires authentication:
 * ```
 * HttpClient(engine) {
 *     install(BearerAuthPlugin) { tokenStorage = myTokenStorage }
 * }
 * ```
 */
class BearerAuthPlugin(private val tokenStorage: TokenStorage) {

    class Config {
        lateinit var tokenStorage: TokenStorage
    }

    companion object : HttpClientPlugin<Config, BearerAuthPlugin> {
        override val key: AttributeKey<BearerAuthPlugin> = AttributeKey("BearerAuth")

        override fun prepare(block: Config.() -> Unit): BearerAuthPlugin =
            BearerAuthPlugin(Config().apply(block).tokenStorage)

        override fun install(plugin: BearerAuthPlugin, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                val token = plugin.tokenStorage.getAccessToken()
                if (token != null) {
                    context.headers.append(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        }
    }
}
