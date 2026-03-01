package com.gdavidpb.tuindice.login.data.source

import com.gdavidpb.tuindice.login.data.model.SubscribeRequest
import com.gdavidpb.tuindice.login.data.repository.LoginMessagingApiDataSource
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

class KtorLoginMessagingApiDataSource(
	private val ktorClient: HttpClient
) : LoginMessagingApiDataSource {
	override suspend fun subscribe(token: String) {
		require(token.isNotBlank()) { "Messaging token cannot be blank." }

		ktorClient.post("messaging") {
			header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
			setBody(
				SubscribeRequest(
					token = token
				)
			)
		}
	}
}
