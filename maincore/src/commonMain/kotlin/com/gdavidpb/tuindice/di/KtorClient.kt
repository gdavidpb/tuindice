package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.auth.domain.model.RefreshTokensAttestationPayload
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.data.source.network.AttestationHeaders
import com.gdavidpb.tuindice.base.data.source.network.createPlatformHttpClient
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.utils.canonicalAttestationPayloadJson
import com.gdavidpb.tuindice.base.utils.extension.isForbidden
import com.gdavidpb.tuindice.base.utils.extension.isLocked
import com.gdavidpb.tuindice.base.utils.extension.isUnauthorized
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.userAgent
import io.ktor.serialization.kotlinx.json.json
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

		install(Logging) {
			this.logger = logger
			level = LogLevel.ALL

			sanitizeHeader { header ->
				header == HttpHeaders.Authorization ||
						header == "X-Forwarded-Authorization" ||
						header == AttestationHeaders.ATTESTATION_TOKEN
			}
		}

		install(Auth) {
			bearer {
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
					val oldAccessToken = oldTokens?.accessToken ?: sessionRepository.getAccessToken()
					val oldRefreshToken = oldTokens?.refreshToken ?: sessionRepository.getRefreshToken()
					val attestationRepository = attestationRepositoryProvider()
					val authRepository = authRepositoryProvider()
					val credentialsRepository = credentialsRepositoryProvider()
					val syncRepository = syncRepositoryProvider()

					val attestationPayload = RefreshTokensAttestationPayload(
						accessToken = oldAccessToken,
						refreshToken = oldRefreshToken
					)

					val attestation = attestationRepository.attest(
						request = AttestationRequest(
							operation = com.gdavidpb.tuindice.base.domain.model.AttestedOperation.RefreshTokens,
							payloadJson = canonicalAttestationPayloadJson(
								serializer = RefreshTokensAttestationPayload.serializer(),
								value = attestationPayload
							)
						)
					)

					val refreshedTokens = runCatching {
						authRepository.refreshTokens(
							accessToken = oldAccessToken,
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
	runCatching { syncStatusRepository.setSyncStatus(SyncStatus.Healthy) }
	runCatching { applicationRepository.clearData() }
	runCatching { sessionInvalidationRepository.notifySessionInvalidated() }
}
