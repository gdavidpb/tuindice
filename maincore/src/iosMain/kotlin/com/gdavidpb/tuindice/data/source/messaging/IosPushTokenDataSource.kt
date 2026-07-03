package com.gdavidpb.tuindice.data.source.messaging

import com.gdavidpb.tuindice.base.data.repository.messaging.PushTokenDataRepository
import com.gdavidpb.tuindice.platform.IosPushCapability

class IosPushTokenDataSource(
	private val pushCapability: IosPushCapability
) : PushTokenDataRepository {
	override suspend fun getToken(): String {
		return pushCapability.pushToken()
			?.takeIf { token -> token.isNotBlank() }
			?: throw IllegalStateException("Push token unavailable on iOS bridge.")
	}
}
