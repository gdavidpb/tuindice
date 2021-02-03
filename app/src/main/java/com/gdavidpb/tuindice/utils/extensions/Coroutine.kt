package com.gdavidpb.tuindice.utils.extensions

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import kotlin.coroutines.suspendCoroutine

suspend fun <T> Task<T>.await() = suspendCoroutine<T> { continuation ->
    runCatching { Tasks.await(this) }.let(continuation::resumeWith)
}