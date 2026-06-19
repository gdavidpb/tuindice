package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.network.AuthErrorHeaders
import com.gdavidpb.tuindice.base.data.source.network.AttestationHeaders
import com.gdavidpb.tuindice.base.data.source.network.createPlatformHttpClient
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.data.source.network.AppUpgradePolicy
import com.gdavidpb.tuindice.data.source.network.UpgradeRequiredResponse
import com.gdavidpb.tuindice.domain.repository.OutdatedAppEventRepository
import com.gdavidpb.tuindice.domain.repository.SessionRecoveryRepository
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.AuthConfig
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
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
					clientRequestException.response.persistOutdatedAppState(
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
	}
}

private suspend fun io.ktor.client.statement.HttpResponse.persistOutdatedAppState(
	settingsRepository: SettingsRepository,
	outdatedAppEventRepository: OutdatedAppEventRepository,
	userAgentValue: String?
) {
	val upgradeRequiredResponse = runCatching {
		body<UpgradeRequiredResponse>()
	}.getOrNull() ?: return

	val outdatedAppState = AppUpgradePolicy.toOutdatedAppState(
		response = upgradeRequiredResponse,
		userAgentValue = userAgentValue
	) ?: return

	settingsRepository.setOutdatedAppState(outdatedAppState)
	outdatedAppEventRepository.notifyOutdatedApp(outdatedAppState)
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

internal fun String.shouldSendBearerAuth(): Boolean {
	val normalizedPath = trimStart('/')
	return !normalizedPath.startsWith("auth/") &&
			!normalizedPath.startsWith("attestation/")
}
