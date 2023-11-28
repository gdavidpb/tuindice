package com.gdavidpb.tuindice.transactions.utils.extension

import com.gdavidpb.tuindice.base.utils.extension.findAnnotation
import com.gdavidpb.tuindice.base.utils.extension.hasAnnotation
import com.gdavidpb.tuindice.transactions.domain.annotation.EnqueuedApi
import okhttp3.Request

fun Request.isEnqueuedApi() = hasAnnotation<EnqueuedApi>()

fun Request.getEnqueued() = findAnnotation<EnqueuedApi>()