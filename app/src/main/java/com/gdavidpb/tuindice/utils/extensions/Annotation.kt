package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.domain.usecase.coroutines.BaseUseCase
import com.gdavidpb.tuindice.utils.annotations.IgnoredFromExceptionReporting
import kotlin.reflect.full.findAnnotation

fun BaseUseCase<*, *, *, *>.isIgnoredFromExceptionReporting(throwable: Throwable) =
        this::class.findAnnotation<IgnoredFromExceptionReporting>()
                ?.run { exceptions.any { it.isInstance(throwable) } }
                ?: false