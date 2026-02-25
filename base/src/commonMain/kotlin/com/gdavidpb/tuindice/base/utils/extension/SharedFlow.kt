package com.gdavidpb.tuindice.base.utils.extension

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first

suspend fun <T> MutableSharedFlow<T>.waitForSubscribers() {
	if (subscriptionCount.value == 0) subscriptionCount.first { count -> count > 0 }
}
