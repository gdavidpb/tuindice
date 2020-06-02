package com.gdavidpb.tuindice.utils.extensions

import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit

inline fun <reified T> Retrofit.create(): T = create(T::class.java) as T

fun <T> Response<T>.getOrThrow(): T = body() ?: throw HttpException(this)