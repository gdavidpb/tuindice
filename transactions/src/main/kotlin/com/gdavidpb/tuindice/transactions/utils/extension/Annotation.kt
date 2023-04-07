package com.gdavidpb.tuindice.transactions.utils.extension

import com.gdavidpb.tuindice.base.utils.extension.hasAnnotation
import com.gdavidpb.tuindice.transactions.domain.annotation.EnqueueOnFailure
import okhttp3.Request

fun Request.isEnqueueOnFailure() = hasAnnotation<EnqueueOnFailure>()