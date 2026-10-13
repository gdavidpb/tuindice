package com.gdavidpb.tuindice.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpHeaders

// What a mock-engine client received, read from the engine's own history once the requests are
// done. A handler runs on the engine's dispatcher with no exclusion, so a collection it fills races
// with the test that reads it as soon as two requests overlap; the history does not.

/** The Authorization header of every request, in the order they arrived; every request must carry one. */
internal fun HttpClient.authorizationHeaders(): List<String> {
	return (engine as MockEngine).requestHistory.map { request ->
		checkNotNull(request.headers[HttpHeaders.Authorization])
	}
}

/** The Authorization header (null when absent) of the last request to each "METHOD /path". */
internal fun HttpClient.authorizationHeadersByRequest(): Map<String, String?> {
	return (engine as MockEngine).requestHistory.associate { request ->
		"${request.method.value} ${request.url.encodedPath}" to request.headers[HttpHeaders.Authorization]
	}
}

/** The Authorization header (null when absent) of the last request to each path. */
internal fun HttpClient.authorizationHeadersByPath(): Map<String, String?> {
	return (engine as MockEngine).requestHistory.associate { request ->
		request.url.encodedPath to request.headers[HttpHeaders.Authorization]
	}
}
