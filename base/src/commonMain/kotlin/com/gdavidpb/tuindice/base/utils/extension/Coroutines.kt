package com.gdavidpb.tuindice.base.utils.extension

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull

private const val TIMEOUT_NO_AWAIT = 10_000L

suspend fun noAwait(block: suspend () -> Unit) {
	coroutineScope {
		runCatching {
			withTimeoutOrNull(TIMEOUT_NO_AWAIT) {
				block()
			}
		}
	}
}
