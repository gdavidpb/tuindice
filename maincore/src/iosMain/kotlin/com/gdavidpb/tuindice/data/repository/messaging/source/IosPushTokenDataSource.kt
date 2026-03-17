package com.gdavidpb.tuindice.data.repository.messaging.source

import com.gdavidpb.tuindice.data.repository.messaging.PushTokenDataSource
import com.gdavidpb.tuindice.platform.IosPushCapability

class IosPushTokenDataSource(
	private val pushCapability: IosPushCapability
) : PushTokenDataSource {
	override suspend fun getToken(): String {
		return pushCapability.pushToken()
			?.takeIf { token -> token.isNotBlank() }
			?: throw IllegalStateException("Push token unavailable on iOS bridge.")
	}
}
