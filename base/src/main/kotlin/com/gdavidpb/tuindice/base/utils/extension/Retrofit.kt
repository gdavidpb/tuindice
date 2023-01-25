package com.gdavidpb.tuindice.base.utils.extension

import okhttp3.Headers
import okhttp3.Request
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit

inline fun <reified T> Retrofit.create(): T {
    return create(T::class.java) as T
}

fun (Request.Builder).headerPutIfAbsent(headers: Headers, name: String, value: String) {
    if (headers[name] == null) header(name, value)
}

fun <T> Response<T>.getOrThrow(): T {
    return body() ?: throw HttpException(this)
}