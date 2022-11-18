package com.gdavidpb.tuindice.data.source

import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import java.util.*

class MessagingMockDataSource : MessagingRepository {

	private val token = UUID.randomUUID().toString()

	override suspend fun getToken(): String {
		return token
	}

	override suspend fun subscribeToTopic(topic: String) {
	}

	override suspend fun unsubscribeFromTopic(topic: String) {
	}
}