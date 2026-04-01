package com.gdavidpb.tuindice.data.source.messaging

import com.gdavidpb.tuindice.base.logging.appLogger
import com.gdavidpb.tuindice.data.repository.messaging.PushTokenDataRepository

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
