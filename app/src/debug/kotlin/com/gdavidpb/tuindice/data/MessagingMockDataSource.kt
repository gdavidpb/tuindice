package com.gdavidpb.tuindice.data

import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository

class MessagingMockDataSource : MessagingRepository {
	override suspend fun subscribe() {
	}

	override suspend fun unsubscribe() {
	}
}