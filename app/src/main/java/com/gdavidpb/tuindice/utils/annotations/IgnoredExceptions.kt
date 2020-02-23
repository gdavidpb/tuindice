package com.gdavidpb.tuindice.utils.annotations

import kotlin.reflect.KClass

object None : Throwable()

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class IgnoredExceptions(vararg val exceptions: KClass<out Throwable> = [None::class])