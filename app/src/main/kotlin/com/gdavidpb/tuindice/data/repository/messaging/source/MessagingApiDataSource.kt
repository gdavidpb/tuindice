package com.gdavidpb.tuindice.data.repository.messaging.source

import com.gdavidpb.tuindice.data.repository.messaging.RemoteDataSource
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.post

class MessagingApiDataSource(
	private val ktorClient: HttpClient
) : RemoteDataSource {
	override suspend fun enroll(messagingToken: String) {
		ktorClient.post("messaging") {
			parameter("token", messagingToken)
		}
	}
}