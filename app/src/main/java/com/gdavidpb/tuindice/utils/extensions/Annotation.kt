package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.utils.annotations.IgnoredExceptions
import kotlin.reflect.full.findAnnotation

fun Any.ignoredException(throwable: Throwable) =
        this::class.findAnnotation<IgnoredExceptions>()
                ?.run { exceptions.contains(throwable::class) }
                ?: false