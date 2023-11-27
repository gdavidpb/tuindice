package com.gdavidpb.tuindice.data.repository.messaging.source

import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.data.repository.messaging.MessagingApi
import com.gdavidpb.tuindice.data.repository.messaging.RemoteDataSource

class MessagingApiDataSource(
	private val messagingApi: MessagingApi
) : RemoteDataSource {
	override suspend fun enroll(messagingToken: String) {
		messagingApi
			.enroll(messagingToken)
			.getOrThrow()
	}
}