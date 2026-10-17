package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.network.createPlatformHttpClient
import com.gdavidpb.tuindice.data.source.network.installOutdatedAppPersistence
import com.gdavidpb.tuindice.security.data.source.network.AttestationHeaders
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

/**
 * The client that streams the assistant's events. It has the engine and the auth of the shared client, and it reads a
 * stream as it arrives: no request timeout, and a socket timeout longer than the server's heartbeat.
 * It does not retry a connection by itself: the SSE data source reconnects from the last event it delivered.
 */
fun createAssistantStreamHttpClient(
	dependencies: AssistantStreamDependencies,
	userAgentValue: String? = null
): HttpClient {
	return createPlatformHttpClient {
		configureAssistantStream(dependencies = dependencies, userAgentValue = userAgentValue)
	}
}

internal fun HttpClientConfig<*>.configureAssistantStream(
	dependencies: AssistantStreamDependencies,
	userAgentValue: String?
) {
	expectSuccess = true

	install(DefaultRequest) {
		val appEnvironment = dependencies.appEnvironmentRepository.getEnvironment()

		url(appEnvironment.apiBaseUrl)
		contentType(ContentType.Application.Json)

		if (!userAgentValue.isNullOrBlank()) {
			header(HttpHeaders.UserAgent, userAgentValue)
		}
	}

	installAssistantStreamTimeouts(timeoutMillis = dependencies.configRepository.getTimeout())

	install(Logging) {
		this.logger = dependencies.logger
		// HEADERS and never ALL: the body of a stream is the conversation.
		level = LogLevel.HEADERS

		sanitizeHeader { header ->
			header == HttpHeaders.Authorization ||
					header == AttestationHeaders.ATTESTATION_TOKEN
		}
	}

	install(Auth) {
		installSharedBearerAuth(
			sessionRepository = dependencies.sessionRepository,
			sessionRecoveryRepository = dependencies.sessionRecoveryRepository
		)
	}

	installCurrentSessionBearerAuth(dependencies.sessionRecoveryRepository)

	installInsufficientScopeSessionInvalidation(
		sessionRepository = dependencies.sessionRepository,
		sessionRecoveryRepository = dependencies.sessionRecoveryRepository,
		sessionCoroutineScope = dependencies.sessionCoroutineScope
	)

	// A stream refused because the app is outdated is recorded as the shared client records it.
	installOutdatedAppPersistence(
		settingsRepository = dependencies.settingsRepository,
		outdatedAppEventRepository = dependencies.outdatedAppEventRepository,
		userAgentValue = userAgentValue
	)

	install(SSE) {
		// The reconnection belongs to the data source, which resumes from the last event it delivered.
		maxReconnectionAttempts = 0
	}
}

// The connection is bounded by the shared timeout; the request is not, since a turn streams as long as the server keeps
// it open. The socket timeout is the silence that counts as a dead connection (see the constant below).
internal fun HttpClientConfig<*>.installAssistantStreamTimeouts(timeoutMillis: Long) {
	install(HttpTimeout) {
		connectTimeoutMillis = timeoutMillis
		socketTimeoutMillis = ASSISTANT_STREAM_SOCKET_TIMEOUT_MILLIS
	}
}

// The server sends a heartbeat every 15 seconds. Three heartbeats lost in a row (45 seconds of silence) end the
// connection, and the data source reconnects from the last event it delivered.
internal const val ASSISTANT_STREAM_SOCKET_TIMEOUT_MILLIS = 45_000L
