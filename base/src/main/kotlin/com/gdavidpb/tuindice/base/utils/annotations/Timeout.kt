package com.gdavidpb.tuindice.base.utils.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Timeout(val key: String)