package com.gdavidpb.tuindice.base.utils.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private const val TIMEOUT_NO_AWAIT = 10000L

fun noAwait(block: suspend () -> Unit) {
	CoroutineScope(Dispatchers.IO).launch {
		runCatching {
			withTimeoutOrNull(TIMEOUT_NO_AWAIT) {
				block()
			}
		}
	}
}