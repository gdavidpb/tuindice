package com.gdavidpb.tuindice.testkit.ktor

import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpResponseData
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.takeFrom
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(InternalAPI::class)
fun clientRequestException(
	statusCode: HttpStatusCode,
	path: String = "/test"
): ClientRequestException {
	val response = httpResponse(statusCode = statusCode, path = path)

	return ClientRequestException(response, statusCode.description)
}

fun serverResponseException(
	statusCode: HttpStatusCode,
	path: String = "/test"
): ServerResponseException {
	val response = httpResponse(statusCode = statusCode, path = path)

	return ServerResponseException(response, statusCode.description)
}

@OptIn(InternalAPI::class)
private fun httpResponse(
	statusCode: HttpStatusCode,
	path: String
): HttpResponse {
	val client = HttpClient(MockEngine { respondOk() })
	val requestData = HttpRequestBuilder().apply {
		url.takeFrom("https://tuindice.test$path")
	}.build()
	val responseData = HttpResponseData(
		statusCode = statusCode,
		requestTime = GMTDate(),
		headers = Headers.Empty,
		version = HttpProtocolVersion.HTTP_1_1,
		body = ByteReadChannel.Empty,
		callContext = EmptyCoroutineContext
	)
	val call = HttpClientCall(client, requestData, responseData)

	return call.response
}
