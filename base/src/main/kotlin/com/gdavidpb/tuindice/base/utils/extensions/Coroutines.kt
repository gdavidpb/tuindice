package com.gdavidpb.tuindice.base.utils.extensions

import com.google.android.play.core.tasks.Tasks
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.suspendCoroutine
import com.google.android.gms.tasks.Task as GMSTask
import com.google.android.play.core.tasks.Task as CoreTask

suspend fun <T> GMSTask<T>.awaitOrNull(): T? = runCatching { await() }.getOrNull()

suspend fun <T> CoreTask<T>.await(): T? = suspendCoroutine { continuation ->
    runCatching { Tasks.await(this) }.also(continuation::resumeWith)
}