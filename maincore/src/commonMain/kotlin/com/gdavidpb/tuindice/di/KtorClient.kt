package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.network.AttestationHeaders
import com.gdavidpb.tuindice.base.data.source.network.createPlatformHttpClient
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.utils.canonicalAttestationPayloadJson
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokensAttestationPayload
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
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

					val response = authRepository.refreshTokens(
						accessToken = oldAccessToken,
						refreshToken = oldRefreshToken,
						attestation = attestation
					)

					if (credentialsRepository.hasPassword()) {
						syncRepository.scheduleSync(
							password = credentialsRepository.getPassword()
						)
					}

					BearerTokens(
						accessToken = response.accessToken,
						refreshToken = response.refreshToken
					)
				}
			}
		}
	}
}
