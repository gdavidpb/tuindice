package com.gdavidpb.tuindice.data

import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository

class MessagingMockDataSource : MessagingRepository {
	override suspend fun enroll() {
	}

	override suspend fun subscribeToTopic(topic: String) {
	}

	override suspend fun unsubscribeFromTopic(topic: String) {
	}

	override suspend fun unsubscribeFromAllTopics() {
	}
}