package com.gdavidpb.tuindice.data.repository.messaging.source

import com.gdavidpb.tuindice.base.logging.appLogger
import com.gdavidpb.tuindice.data.repository.messaging.PushTokenDataSource

class DebugPushTokenDataSource(
	private val token: String,
	private val sourceName: String
) : PushTokenDataSource {
	private val logger = appLogger(tag = "PushToken")

	override suspend fun getToken(): String {
		logger.i { "[$sourceName] getToken(): returning debug token instead of Firebase Messaging/APNs." }
		return token
	}
}
