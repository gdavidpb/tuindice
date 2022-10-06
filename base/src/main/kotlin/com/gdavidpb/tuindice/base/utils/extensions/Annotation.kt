package com.gdavidpb.tuindice.base.utils.extensions

import com.gdavidpb.tuindice.base.domain.usecase.base.BaseUseCase
import com.gdavidpb.tuindice.base.utils.annotations.Timeout
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

fun BaseUseCase<*, *, *, *>.hasTimeoutKey() = this::class.hasAnnotation<Timeout>()

fun BaseUseCase<*, *, *, *>.getTimeoutKey() = this::class.findAnnotation<Timeout>()!!.key