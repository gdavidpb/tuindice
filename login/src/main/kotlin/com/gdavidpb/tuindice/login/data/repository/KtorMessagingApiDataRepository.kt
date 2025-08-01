package com.gdavidpb.tuindice.login.data.repository

import com.gdavidpb.tuindice.login.domain.repository.MessagingApiRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.post

class KtorMessagingApiDataRepository(
	private val ktorClient: HttpClient
) : MessagingApiRepository {
	override suspend fun subscribe(token: String) {
		ktorClient.post("messaging") {
			parameter("token", token)
		}
	}
}