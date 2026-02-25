package com.gdavidpb.tuindice.base.utils.extension

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

suspend fun <T> MutableSharedFlow<T>.waitForSubscribers(timeoutMillis: Long = 500L) {
	if (subscriptionCount.value > 0) return

	withTimeoutOrNull(timeoutMillis) {
		subscriptionCount.first { count -> count > 0 }
	}
}
