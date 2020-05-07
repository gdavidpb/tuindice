package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.domain.model.exception.SynchronizationException
import com.gdavidpb.tuindice.utils.HEADER_DATE
import com.gdavidpb.tuindice.utils.TIME_SYNCHRONIZATION
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import java.util.*
import kotlin.math.abs

inline fun <reified T> Retrofit.create(): T = create(T::class.java) as T

fun <T> Response<T>.getOrThrow(): T {
    val isSynchronized = isSynchronized(delta = TIME_SYNCHRONIZATION)

    return if (isSynchronized)
        body() ?: throw HttpException(this)
    else
        throw SynchronizationException()
}

fun Response<*>.isSynchronized(delta: Long): Boolean {
    val remoteDate = headers().getDate(HEADER_DATE) ?: return true

    val localDate = Date()
    val deltaTime = abs(localDate.time - remoteDate.time)

    return deltaTime <= delta
}