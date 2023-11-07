package com.gdavidpb.tuindice.base.utils.extension

import com.google.android.play.core.tasks.Tasks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.suspendCoroutine
import com.google.android.gms.tasks.Task as GMSTask
import com.google.android.play.core.tasks.Task as CoreTask

private const val TIMEOUT_NO_AWAIT = 10000L

suspend fun <T> GMSTask<T>.awaitOrNull(): T? = runCatching { await() }.getOrNull()

suspend fun <T> CoreTask<T>.await(): T? = suspendCoroutine { continuation ->
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