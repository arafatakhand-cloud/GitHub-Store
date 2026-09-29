package zed.rainxch.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.darwin.Darwin
import io.ktor.http.Url
import zed.rainxch.core.domain.model.ProxyConfig

actual fun createPlatformHttpClient(proxyConfig: ProxyConfig): HttpClient =
    HttpClient(Darwin) {
        engine {
            // NSURLSession follows the system proxy settings by default, which covers
            // ProxyConfig.System. SOCKS proxies are not supported by the Darwin engine.
            when (proxyConfig) {
                is ProxyConfig.Http -> {
                    proxy = ProxyBuilder.http(Url("http://${proxyConfig.host}:${proxyConfig.port}"))
                }

                is ProxyConfig.None,
                is ProxyConfig.System,
                is ProxyConfig.Socks,
                -> {}
            }
        }
    }
