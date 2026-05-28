package com.gdavidpb.tuindice.data.source.messaging

import com.gdavidpb.tuindice.data.repository.messaging.MessagingRemoteDataRepository
import com.gdavidpb.tuindice.data.source.messaging.api.request.SubscribeRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class MessagingApiDataSource(
	private val ktorClient: HttpClient
) : MessagingRemoteDataRepository {
	override suspend fun subscribe(messagingToken: String) {
		ktorClient.post("messaging/v1") {
			setBody(
				SubscribeRequest(
					token = messagingToken
				)
			)
		}
	}

	override suspend fun unsubscribe() {
		ktorClient.delete("messaging/v1")
	}
}
