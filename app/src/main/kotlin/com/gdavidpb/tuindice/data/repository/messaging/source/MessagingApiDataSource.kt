package com.gdavidpb.tuindice.data.repository.messaging.source

import com.gdavidpb.tuindice.data.repository.messaging.RemoteDataSource
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.parameter
import io.ktor.client.request.post

class MessagingApiDataSource(
	private val ktorClient: HttpClient
) : RemoteDataSource {
	override suspend fun subscribe(messagingToken: String) {
		ktorClient.post("messaging") {
			parameter("token", messagingToken)
		}
	}

	override suspend fun unsubscribe() {
		ktorClient.delete("messaging")
	}
}