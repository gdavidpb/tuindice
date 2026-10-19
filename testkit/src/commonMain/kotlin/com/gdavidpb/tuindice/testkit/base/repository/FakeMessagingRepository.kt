package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository

class FakeMessagingRepository(
	var throwable: Throwable? = null
) : MessagingRepository {
	var subscribeCalls = 0
	var unsubscribeCalls = 0

	override suspend fun subscribe() {
		subscribeCalls++

		throwable?.let { throw it }
	}

	override suspend fun unsubscribe() {
		unsubscribeCalls++
	}
}
