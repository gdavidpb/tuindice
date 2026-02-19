package com.gdavidpb.tuindice.data.repository.messaging.source

import com.gdavidpb.tuindice.data.repository.messaging.RemoteDataSource
import com.gdavidpb.tuindice.data.repository.messaging.source.api.request.SubscribeRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class MessagingApiDataSource(
	private val ktorClient: HttpClient
) : RemoteDataSource {
	override suspend fun subscribe(messagingToken: String) {
		ktorClient.post("messaging") {
			setBody(
				SubscribeRequest(
					token = messagingToken
				)
			)
		}
	}

	override suspend fun unsubscribe() {
		ktorClient.delete("messaging")
	}
}