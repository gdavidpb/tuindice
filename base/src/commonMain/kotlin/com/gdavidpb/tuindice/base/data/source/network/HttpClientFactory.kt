package com.gdavidpb.tuindice.base.data.source.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

fun createPlatformHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient {
	return HttpClient(block)
}
