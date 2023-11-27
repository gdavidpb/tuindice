package com.gdavidpb.tuindice.base.utils.extension

import com.google.android.play.core.tasks.Task
import com.google.android.play.core.tasks.Tasks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.suspendCoroutine

private const val TIMEOUT_NO_AWAIT = 10000L

suspend fun <T> Task<T>.await(): T? = suspendCoroutine { continuation ->
	runCatching { Tasks.await(this) }.also(continuation::resumeWith)
}

fun noAwait(block: suspend () -> Unit) {
	CoroutineScope(Dispatchers.IO).launch {
		runCatching {
			withTimeoutOrNull(TIMEOUT_NO_AWAIT) {
				block()
			}
		}
	}
}