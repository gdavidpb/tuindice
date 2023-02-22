package com.gdavidpb.tuindice.base.utils.extension

import kotlinx.coroutines.TimeoutCancellationException
import retrofit2.HttpException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.HttpURLConnection
import java.net.SocketException
import java.net.UnknownHostException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLException

fun Throwable.isUnavailable() = when (this) {
	is HttpException -> (code() == HttpURLConnection.HTTP_UNAVAILABLE)
	else -> false
}

fun Throwable.isForbidden() = when (this) {
	is HttpException -> (code() == HttpURLConnection.HTTP_FORBIDDEN)
	else -> false
}

fun Throwable.isConflict() = when (this) {
	is HttpException -> (code() == HttpURLConnection.HTTP_CONFLICT)
	else -> false
}

fun Throwable.isUnauthorized() = when (this) {
	is HttpException -> (code() == HttpURLConnection.HTTP_UNAUTHORIZED)
	else -> false
}

fun Throwable.isNotFound() = when (this) {
	is HttpException -> (code() == HttpURLConnection.HTTP_NOT_FOUND)
	else -> false
}

fun Throwable.isTimeout() = when (this) {
	is TimeoutException -> true
	is TimeoutCancellationException -> true
	else -> false
}

fun Throwable.isIO() = when (this) {
	is IOException -> true
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