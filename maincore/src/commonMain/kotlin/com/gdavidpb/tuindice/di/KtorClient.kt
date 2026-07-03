package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.network.AuthErrorHeaders
import com.gdavidpb.tuindice.base.data.source.network.createPlatformHttpClient
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.data.source.network.persistOutdatedAppStateIfUpgradeRequired
import com.gdavidpb.tuindice.domain.repository.OutdatedAppEventRepository
import com.gdavidpb.tuindice.domain.repository.SessionRecoveryRepository
import com.gdavidpb.tuindice.security.data.source.network.AttestationHeaders
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.api.SendingRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.AuthConfig
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun createSharedJson(): Json {
	return Json {
		explicitNulls = true
		ignoreUnknownKeys = true
		prettyPrint = false
	}
}

fun createSharedHttpClient(
	appEnvironmentRepository: AppEnvironmentRepository,
	configRepository: ConfigRepository,
	sessionRepository: SessionRepository,
	settingsRepository: SettingsRepository,
	sessionRecoveryRepository: SessionRecoveryRepository,
	outdatedAppEventRepository: OutdatedAppEventRepository,
	logger: Logger,
	json: Json,
	userAgentValue: String? = null
): HttpClient {
	return createPlatformHttpClient {
		expectSuccess = true

		install(DefaultRequest) {
			val appEnvironment = appEnvironmentRepository.getEnvironment()

			url(appEnvironment.apiBaseUrl)
			contentType(ContentType.Application.Json)

			if (!userAgentValue.isNullOrBlank()) {
				userAgent(userAgentValue)
			}
		}

		install(HttpTimeout) {
			val timeout = configRepository.getTimeout()

			requestTimeoutMillis = timeout
			connectTimeoutMillis = timeout
			socketTimeoutMillis = timeout
		}

		install(ContentNegotiation) {
			json(json)
		}

		HttpResponseValidator {
			handleResponseExceptionWithRequest { exception, _ ->
				val clientRequestException = exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest
				val authError = clientRequestException.response.headers[AuthErrorHeaders.HEADER]

				if (
					clientRequestException.response.status == HttpStatusCode.Forbidden &&
					authError == AuthErrorHeaders.INSUFFICIENT_SCOPE
				) {
					sessionRecoveryRepository.invalidateSession(
						sessionId = sessionRepository.getActiveSessionSnapshot()?.sessionId
					)
				}

				if (clientRequestException.response.status == HttpStatusCode.UpgradeRequired) {
					exception.persistOutdatedAppStateIfUpgradeRequired(
						settingsRepository = settingsRepository,
						outdatedAppEventRepository = outdatedAppEventRepository,
						userAgentValue = userAgentValue
					)
				}
			}
		}

		install(Logging) {
			this.logger = logger
			level = LogLevel.ALL

			sanitizeHeader { header ->
				header == HttpHeaders.Authorization ||
						header == AttestationHeaders.ATTESTATION_TOKEN
			}
		}

		install(Auth) {
			installSharedBearerAuth(
				sessionRepository = sessionRepository,
				sessionRecoveryRepository = sessionRecoveryRepository
			)
		}

		installCurrentSessionBearerAuth(sessionRepository)
	}
}

internal fun AuthConfig.installSharedBearerAuth(
	sessionRepository: SessionRepository,
	sessionRecoveryRepository: SessionRecoveryRepository
) {
	reAuthorizeOnResponse { response ->
		response.status == HttpStatusCode.Unauthorized &&
				response.call.request.url.encodedPath.shouldSendBearerAuth()
	}

	bearer {
		sendWithoutRequest { request ->
			request.url.encodedPath.shouldSendBearerAuth()
		}

		loadTokens {
			sessionRepository.getActiveSessionSnapshot()?.toBearerTokens()
		}

		// SessionRepository already owns token persistence and mutation.
		cacheTokens = false

		refreshTokens {
			val attemptedAccessToken = response.call.request.headers[HttpHeaders.Authorization]?.bearerAccessToken()

			sessionRecoveryRepository.recoverUnauthorizedSession(
				attemptedAuthorizationAccessToken = attemptedAccessToken,
				attemptedCachedAccessToken = oldTokens?.accessToken,
				attemptedCachedRefreshToken = oldTokens?.refreshToken
			)?.toBearerTokens()
		}
	}
}

internal fun HttpClientConfig<*>.installCurrentSessionBearerAuth(
	sessionRepository: SessionRepository
) {
	install(CurrentSessionBearerAuth) {
		this.sessionRepository = sessionRepository
	}
}

private class CurrentSessionBearerAuthConfig {
	lateinit var sessionRepository: SessionRepository
}

private val CurrentSessionBearerAuth = createClientPlugin(
	name = "CurrentSessionBearerAuth",
	createConfiguration = ::CurrentSessionBearerAuthConfig
) {
	val sessionRepository = pluginConfig.sessionRepository

	on(SendingRequest) { request, _ ->
		request.attachCurrentSessionBearerAuth(sessionRepository)
	}
}

private fun String.bearerAccessToken(): String? {
	val prefix = "Bearer "
	return takeIf { authorization -> authorization.startsWith(prefix) }
		?.removePrefix(prefix)
}

private fun SessionSnapshot.toBearerTokens(): BearerTokens {
	return BearerTokens(
		accessToken = accessToken,
		refreshToken = refreshToken
	)
}

private suspend fun HttpRequestBuilder.attachCurrentSessionBearerAuth(
	sessionRepository: SessionRepository
) {
	if (!url.encodedPath.shouldSendBearerAuth()) return

	val snapshot = sessionRepository.getActiveSessionSnapshot() ?: return

	headers.remove(HttpHeaders.Authorization)
	headers.append(HttpHeaders.Authorization, "Bearer ${snapshot.accessToken}")
}

internal fun String.shouldSendBearerAuth(): Boolean {
	val normalizedPath = trimStart('/')
	return !normalizedPath.startsWith("auth/") &&
			!normalizedPath.startsWith("attestation/")
}
