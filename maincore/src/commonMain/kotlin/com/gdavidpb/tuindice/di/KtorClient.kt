package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.auth.domain.model.RefreshTokensAttestationPayload
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.data.source.network.AuthErrorHeaders
import com.gdavidpb.tuindice.base.data.source.network.AttestationHeaders
import com.gdavidpb.tuindice.base.data.source.network.createPlatformHttpClient
import com.gdavidpb.tuindice.base.domain.model.AttestationAuthorization
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest
import com.gdavidpb.tuindice.base.domain.model.ProtectedOperationCodes
import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.base.utils.canonicalAttestationPayloadJson
import com.gdavidpb.tuindice.base.utils.extension.isForbidden
import com.gdavidpb.tuindice.base.utils.extension.isLocked
import com.gdavidpb.tuindice.base.utils.extension.isUnauthorized
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun createSharedJson(): Json {
	return Json {
		explicitNulls = true
		prettyPrint = false
	}
}

fun createSharedHttpClient(
	appEnvironmentRepository: AppEnvironmentRepository,
	configRepository: ConfigRepository,
	sessionRepository: SessionRepository,
	applicationRepository: ApplicationRepository,
	sessionInvalidationRepository: SessionInvalidationRepository,
	syncStatusRepository: SyncStatusRepository,
	attestationRepositoryProvider: () -> AttestationRepository,
	authRepositoryProvider: () -> AuthRepository,
	credentialsRepositoryProvider: () -> CredentialsRepository,
	syncRepositoryProvider: () -> SyncRepository,
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
					handleUnauthorizedTokenRefresh(
						sessionRepository = sessionRepository,
						syncStatusRepository = syncStatusRepository,
						applicationRepository = applicationRepository,
						sessionInvalidationRepository = sessionInvalidationRepository
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
			bearer {
				sendWithoutRequest { request ->
					request.url.encodedPath.shouldSendBearerAuth()
				}

				loadTokens {
					val hasActiveTokens = sessionRepository.hasActiveSession()

					if (hasActiveTokens) {
						BearerTokens(
							accessToken = sessionRepository.getAccessToken(),
							refreshToken = sessionRepository.getRefreshToken()
						)
					} else {
						null
					}
				}

				refreshTokens {
					val oldSessionId = sessionRepository.getSessionId()
					val oldRefreshToken = oldTokens?.refreshToken ?: sessionRepository.getRefreshToken()
					val attestationRepository = attestationRepositoryProvider()
					val authRepository = authRepositoryProvider()
					val credentialsRepository = credentialsRepositoryProvider()
					val syncRepository = syncRepositoryProvider()

					val attestationPayload = RefreshTokensAttestationPayload(
						sessionId = oldSessionId,
						refreshToken = oldRefreshToken
					)

					val attestation = attestationRepository.attest(
						request = AttestationRequest(
							operationCode = ProtectedOperationCodes.AuthRefreshTokens,
							payloadJson = canonicalAttestationPayloadJson(
								serializer = RefreshTokensAttestationPayload.serializer(),
								value = attestationPayload
							),
							authorization = AttestationAuthorization.Session(
								sessionId = oldSessionId,
								refreshToken = oldRefreshToken
							)
						)
					)

					val refreshedTokens = runCatching {
						authRepository.refreshTokens(
							sessionId = oldSessionId,
							refreshToken = oldRefreshToken,
							attestation = attestation
						)
					}.getOrElse { throwable ->
						if (!throwable.isSessionInvalidatingRefreshFailure()) throw throwable

						handleUnauthorizedTokenRefresh(
							sessionRepository = sessionRepository,
							syncStatusRepository = syncStatusRepository,
							applicationRepository = applicationRepository,
							sessionInvalidationRepository = sessionInvalidationRepository
						)

						return@refreshTokens null
					}

					if (credentialsRepository.hasPassword()) {
						syncRepository.scheduleSync(
							password = credentialsRepository.getPassword()
						)
					}

					BearerTokens(
						accessToken = refreshedTokens.accessToken,
						refreshToken = refreshedTokens.refreshToken
					)
				}
			}
		}
	}
}

private fun String.shouldSendBearerAuth(): Boolean {
	return !startsWith("/auth/v2/token/refresh") &&
			!startsWith("/auth/v2/token/revoke") &&
			!startsWith("/attestation/v4/session-auth/")
}

internal fun Throwable.isSessionInvalidatingRefreshFailure(): Boolean {
	return isUnauthorized() || isForbidden() || isLocked()
}

internal suspend fun handleUnauthorizedTokenRefresh(
	sessionRepository: SessionRepository,
	syncStatusRepository: SyncStatusRepository,
	applicationRepository: ApplicationRepository,
	sessionInvalidationRepository: SessionInvalidationRepository
) {
	runCatching { sessionRepository.clear() }
	runCatching { syncStatusRepository.reset() }
	runCatching { applicationRepository.clearData() }
	runCatching { sessionInvalidationRepository.notifySessionInvalidated() }
}
