package com.gdavidpb.tuindice.login.data.source

import com.gdavidpb.tuindice.login.data.repository.MessagingApiDataSource
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.post

class KtorMessagingApiApiDataSource(
	private val ktorClient: HttpClient
) : MessagingApiDataSource {
	override suspend fun subscribe(token: String) {
		ktorClient.post("messaging") {
			parameter("token", token)
		}
	}
}