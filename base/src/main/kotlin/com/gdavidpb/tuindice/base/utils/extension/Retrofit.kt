package com.gdavidpb.tuindice.base.utils.extension

import okhttp3.Request
import retrofit2.HttpException
import retrofit2.Invocation
import retrofit2.Response
import retrofit2.Retrofit

inline fun <reified T> Retrofit.create(): T = create(T::class.java) as T

fun <T> Response<T>.getOrThrow(): T = body() ?: throw HttpException(this)

inline fun <reified T : Annotation> Request.findAnnotation() =
	tag(Invocation::class.java)
		?.method()
		?.getAnnotation(T::class.java)

inline fun <reified T : Annotation> Request.hasAnnotation() =
	findAnnotation<T>() != null

inline fun <reified T> Request.bodyAs() =
	tag(Invocation::class.java)
		?.arguments()
		?.filterIsInstance<T>()
		?.firstOrNull()

fun Request.bodyToString(): String? {
	return body?.run {
		okio.Buffer().use { buffer ->
			writeTo(buffer)
			buffer.readUtf8()
		}
	}
}