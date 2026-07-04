package com.gdavidpb.tuindice.base.data.source.messaging

import com.gdavidpb.tuindice.base.data.repository.messaging.PushTokenDataRepository
import com.gdavidpb.tuindice.base.logging.appLogger

class DebugPushTokenDataSource(
	private val token: String,
	private val sourceName: String
) : PushTokenDataRepository {
	private val logger = appLogger(tag = "PushToken")

	override suspend fun getToken(): String {
		logger.i { "[$sourceName] getToken(): returning debug token instead of Firebase Messaging/APNs." }
		return token
	}
}
