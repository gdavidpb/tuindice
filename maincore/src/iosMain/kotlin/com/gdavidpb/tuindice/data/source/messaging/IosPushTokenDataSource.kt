package com.gdavidpb.tuindice.data.source.messaging

import com.gdavidpb.tuindice.data.contract.messaging.PushTokenDataSource
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
