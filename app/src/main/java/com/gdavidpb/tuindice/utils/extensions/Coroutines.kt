package com.gdavidpb.tuindice.utils.extensions

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await

suspend fun <T> Task<T>.awaitCatching(): T? = runCatching { await() }.getOrNull()