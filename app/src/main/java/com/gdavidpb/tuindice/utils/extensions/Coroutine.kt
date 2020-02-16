package com.gdavidpb.tuindice.utils.extensions

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import kotlin.coroutines.suspendCoroutine

@JvmName("awaitVoid")
suspend fun Task<Void>.await() = suspendCoroutine<Unit> { continuation ->
    runCatching { Tasks.await(this) }.map { Unit }.also(continuation::resumeWith)
}

@JvmName("awaitTResult")
suspend fun <T> Task<T>.await() = suspendCoroutine<T> { continuation ->
    runCatching { Tasks.await(this) }.also(continuation::resumeWith)
}