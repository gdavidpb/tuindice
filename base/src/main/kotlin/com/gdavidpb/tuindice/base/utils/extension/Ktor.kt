package com.gdavidpb.tuindice.base.utils.extension

import com.gdavidpb.tuindice.base.domain.exception.HttpException
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

suspend inline fun <reified T> HttpResponse.getOrThrow(): T {
	return if (status.isSuccess())
		body<T>()
	else
		throw HttpException(
			code = status.value,
			message = status.description,
			response = runCatching { bodyAsText() }.getOrNull()
		)
}