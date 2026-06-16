package com.gdavidpb.tuindice.base.utils.extension

import com.gdavidpb.tuindice.base.data.source.network.AuthErrorHeaders
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.TimeoutCancellationException

private val timeoutSimpleClassNames = setOf(
	"TimeoutException",
	"SocketTimeoutException",
	"ConnectTimeoutException",
	"HttpRequestTimeoutException"
)

private val timeoutQualifiedClassNames = setOf(
	"java.util.concurrent.TimeoutException",
	"java.net.SocketTimeoutException",
	"io.ktor.client.network.sockets.ConnectTimeoutException",
	"io.ktor.client.plugins.HttpRequestTimeoutException"
)

private val timeoutMessageFragments = setOf(
	"timeout",
	"timed out",
	"time out"
)

private val connectionSimpleClassNames = setOf(
	"SocketException",
	"InterruptedIOException",
	"UnknownHostException",
	"SSLException",
	"ExecutionException",
	"DarwinHttpRequestException",
	"ConnectException",
	"UnresolvedAddressException",
	"PosixException",
	"NSErrorException"
)

private val connectionQualifiedClassNames = setOf(
	"java.net.SocketException",
	"java.io.InterruptedIOException",
	"java.net.UnknownHostException",
	"javax.net.ssl.SSLException",
	"java.util.concurrent.ExecutionException",
	"io.ktor.client.engine.darwin.DarwinHttpRequestException",
	"io.ktor.client.network.sockets.ConnectException"
)

private val connectionMessageFragments = setOf(
	"network is unreachable",
	"network unreachable",
	"connection was lost",
	"not connected to internet",
	"internet connection appears to be offline",
	"unable to resolve host",
	"could not connect to the server",
	"host unreachable",
	"name or service not known",
	"dns"
)

fun Throwable.isUnavailable() = when (this) {
	is ResponseException -> response.status == HttpStatusCode.ServiceUnavailable
	else -> false
}

fun Throwable.isFailedDependency() = when (this) {
	is ResponseException -> response.status == HttpStatusCode.FailedDependency
	else -> false
}

fun Throwable.isServerError() = when (this) {
	is ResponseException -> response.status.value in 500..599
	else -> false
}

fun Throwable.isTooManyRequests() = when (this) {
	is ResponseException -> response.status == HttpStatusCode.TooManyRequests
	else -> false
}

fun Throwable.isSyncRetryable(): Boolean {
	return isUnavailable() || isFailedDependency() || isTooManyRequests() || isServerError() || isConnection() || isTimeout()
}

fun Throwable.isAccessRejected(): Boolean {
	return isUnauthorized() || isForbidden() || isLocked()
}

fun Throwable.authErrorCode(): String? = when (this) {
	is ResponseException -> response.headers[AuthErrorHeaders.HEADER]
	else -> null
}

fun Throwable.isSessionSuperseded(): Boolean {
	return authErrorCode() == AuthErrorHeaders.SESSION_SUPERSEDED
}

fun Throwable.isRefreshTokenMismatch(): Boolean {
	return authErrorCode() == AuthErrorHeaders.REFRESH_TOKEN_MISMATCH
}

fun Throwable.isForbidden() = when (this) {
	is ResponseException -> response.status == HttpStatusCode.Forbidden
	else -> false
}

fun Throwable.isLocked() = when (this) {
	is ResponseException -> response.status == HttpStatusCode.Locked
	else -> false
}

fun Throwable.isConflict() = when (this) {
	is ResponseException -> response.status == HttpStatusCode.Conflict
	else -> false
}

fun Throwable.isUnauthorized() = when (this) {
	is ResponseException -> response.status == HttpStatusCode.Unauthorized
	else -> false
}

fun Throwable.isNotFound() = when (this) {
	is ResponseException -> response.status == HttpStatusCode.NotFound
	else -> false
}

fun Throwable.isPreconditionFailed() = when (this) {
	is ResponseException -> response.status == HttpStatusCode.PreconditionFailed
	else -> false
}

fun Throwable.isPreconditionRequired() = when (this) {
	is ResponseException -> response.status.value == 428
	else -> false
}

fun Throwable.isPayloadTooLarge() = when (this) {
	is ResponseException -> response.status == HttpStatusCode.PayloadTooLarge
	else -> false
}

fun Throwable.isUnsupportedMediaType() = when (this) {
	is ResponseException -> response.status == HttpStatusCode.UnsupportedMediaType
	else -> false
}

fun Throwable.isTimeout(): Boolean {
	return errorChain().any { throwable ->
		throwable is TimeoutCancellationException ||
				throwable.matchesErrorClass(
					simpleClassNames = timeoutSimpleClassNames,
					qualifiedClassNames = timeoutQualifiedClassNames
				) ||
				throwable.matchesMessage(timeoutMessageFragments)
	}
}

fun Throwable.isConnection(): Boolean {
	return isTimeout() || errorChain().any { throwable ->
		throwable.matchesErrorClass(
			simpleClassNames = connectionSimpleClassNames,
			qualifiedClassNames = connectionQualifiedClassNames
		) ||
				throwable.matchesMessage(connectionMessageFragments)
	}
}

private fun Throwable.errorChain(maxDepth: Int = 8): Sequence<Throwable> = sequence {
	var current: Throwable? = this@errorChain
	var depth = 0

	while (current != null && depth < maxDepth) {
		yield(current)
		current = current.cause
		depth++
	}
}

private fun Throwable.matchesErrorClass(
	simpleClassNames: Set<String>,
	qualifiedClassNames: Set<String>
): Boolean {
	val simpleName = this::class.simpleName
	val qualifiedName = this::class.qualifiedName

	return (simpleName != null && simpleName in simpleClassNames) ||
			(qualifiedName != null && qualifiedName in qualifiedClassNames)
}

private fun Throwable.matchesMessage(keywords: Set<String>): Boolean {
	val lowerMessage = message?.lowercase() ?: return false
	return keywords.any { keyword -> lowerMessage.contains(keyword) }
}
