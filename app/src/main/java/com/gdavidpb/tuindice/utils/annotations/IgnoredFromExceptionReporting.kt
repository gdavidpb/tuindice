package com.gdavidpb.tuindice.utils.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class IgnoredFromExceptionReporting(vararg val exceptions: KClass<out Throwable> = [])