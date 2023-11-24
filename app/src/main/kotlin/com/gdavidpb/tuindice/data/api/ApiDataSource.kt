package com.gdavidpb.tuindice.data.api

import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.data.messaging.source.RemoteDataSource

class ApiDataSource(
	private val messagingApi: MessagingApi
) : RemoteDataSource {
	override suspend fun enroll(messagingToken: String) {
		messagingApi
			.enroll(messagingToken)
			.getOrThrow()
	}
}