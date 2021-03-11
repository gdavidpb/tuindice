package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.domain.usecase.coroutines.BaseUseCase
import com.gdavidpb.tuindice.utils.annotations.Timeout
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

fun BaseUseCase<*, *, *, *>.hasTimeoutKey() = this::class.hasAnnotation<Timeout>()

fun BaseUseCase<*, *, *, *>.getTimeoutKey() = this::class.findAnnotation<Timeout>()!!.key