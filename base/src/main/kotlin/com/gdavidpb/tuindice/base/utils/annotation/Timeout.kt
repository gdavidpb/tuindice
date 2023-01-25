package com.gdavidpb.tuindice.base.utils.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Timeout(val key: String)