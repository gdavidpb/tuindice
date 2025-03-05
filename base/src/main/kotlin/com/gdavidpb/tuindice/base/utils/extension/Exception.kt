package com.gdavidpb.tuindice.base.utils.extension

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.TimeoutCancellationException
import java.io.InterruptedIOException
import java.net.SocketException
import java.net.UnknownHostException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLException

fun Throwable.isUnavailable() = when (this) {
	is ClientRequestException -> (response.status == HttpStatusCode.ServiceUnavailable)
	else -> false
}

fun Throwable.isForbidden() = when (this) {
	is ClientRequestException -> (response.status == HttpStatusCode.Forbidden)
	else -> false
}

fun Throwable.isConflict() = when (this) {
	is ClientRequestException -> (response.status == HttpStatusCode.Conflict)
	else -> false
}

fun Throwable.isUnauthorized() = when (this) {
	is ClientRequestException -> (response.status == HttpStatusCode.Unauthorized)
	else -> false
}

fun Throwable.isNoContent() = when (this) {
	is ClientRequestException -> (response.status == HttpStatusCode.NoContent)
	else -> false
}

fun Throwable.isNotFound() = when (this) {
	is ClientRequestException -> (response.status == HttpStatusCode.NotFound)
	else -> false
}

fun Throwable.isTimeout() = when (this) {
	is TimeoutException -> true
	is TimeoutCancellationException -> true
	else -> false
}

fun Throwable.isConnection() = when (this) {
	is SocketException -> true
	is InterruptedIOException -> true
	is UnknownHostException -> true
	is SSLException -> true
	is ExecutionException -> true
	is TimeoutException -> true
	is TimeoutCancellationException -> true
	else -> false
}