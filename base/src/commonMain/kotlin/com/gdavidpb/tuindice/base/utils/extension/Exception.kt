package com.gdavidpb.tuindice.base.utils.extension

import com.gdavidpb.tuindice.base.data.source.network.AuthErrorHeaders
import com.gdavidpb.tuindice.base.domain.exception.ServiceRetryWindowException
import com.gdavidpb.tuindice.base.domain.exception.SessionRecoveryAttestationException
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
	// A call held back inside the wait the server asked for stands in for the 503 that caused it.
	is ServiceRetryWindowException -> true
	else -> false
}

private val retryableLaterStatusCodes = setOf(
	HttpStatusCode.UpgradeRequired,
	HttpStatusCode.TooManyRequests,
	HttpStatusCode.BadGateway,
	HttpStatusCode.ServiceUnavailable,
	HttpStatusCode.GatewayTimeout
)

/**
 * The failure says nothing about the request itself, so trying it again later can work. Decided by
 * the status code or the exception type, never by the message. What our backend means by each:
 * - 503: Mongo is unreachable, or the auth service is down while it validates the token.
 * - 502 and 504: not emitted by our code, only by the API Gateway in front of Cloud Run; the
 *   evaluations and record-change routes use the default deadline, which a cold start can exceed.
 * - 429: only the auth and attestation rate limits emit it; it reaches a request because the token
 *   refresh happens inside the same send and its error comes out through it.
 * - 426: the minimum-version check, on every route; the request is valid and goes through once the
 *   person updates the app.
 * - Retry window: the call was held back inside the wait the server asked for.
 * - Attestation refused during session recovery: an integrity-plumbing failure, not a verdict.
 * Not here: 400, 401, 403, 423, 500 and the rest, which are answers about the request.
 */
fun Throwable.isRetryableLater(): Boolean = when {
	this is ResponseException -> response.status in retryableLaterStatusCodes
	this is ServiceRetryWindowException -> true
	else -> errorChain().any { throwable -> throwable is SessionRecoveryAttestationException }
}

/**
 * The failure of a send that leaves a queued change waiting for the next attempt. A response from the
 * server is decided by its status code alone ([isRetryableLater]): Ktor puts the response body in the
 * exception message, so reading the message would turn a 400 or a 500 whose body mentions a timeout
 * into an outage. What is not a response (a lost connection, a deadline, a refused attestation) is
 * decided by [isConnection] or [isRetryableLater].
 */
fun Throwable.isTransient(): Boolean = when (this) {
	is ResponseException -> isRetryableLater()
	else -> isConnection() || isRetryableLater()
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

fun Throwable.isUpgradeRequired() = when (this) {
	is ResponseException -> response.status == HttpStatusCode.UpgradeRequired
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
