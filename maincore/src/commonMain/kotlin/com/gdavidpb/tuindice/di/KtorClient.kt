package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.network.createPlatformHttpClient
import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentGateway
import com.gdavidpb.tuindice.base.domain.repository.ConfigGateway
import com.gdavidpb.tuindice.base.domain.repository.IntegrityGateway
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.login.domain.model.IssueTokensAttestationPayload
import com.gdavidpb.tuindice.login.domain.model.RefreshTokensAttestationPayload
import com.gdavidpb.tuindice.login.domain.repository.AuthApiRepository
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
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

fun createSharedJson(): Json {
	return Json {
		explicitNulls = true
		prettyPrint = false

		serializersModule = SerializersModule {
			polymorphic(AttestationPayload::class) {
				subclass(IssueTokensAttestationPayload::class)
				subclass(RefreshTokensAttestationPayload::class)
			}
		}
	}
}

fun createSharedHttpClient(
	appEnvironmentRepository: AppEnvironmentGateway,
	configRepository: ConfigGateway,
	sessionRepository: SessionRepository,
	attestationRepositoryProvider: () -> IntegrityGateway,
	authApiRepositoryProvider: () -> AuthApiRepository,
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
				header == HttpHeaders.Authorization
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
					val authApiRepository = authApiRepositoryProvider()

					val attestationPayload = RefreshTokensAttestationPayload(
						accessToken = oldAccessToken,
						refreshToken = oldRefreshToken
					)

					val attestation = attestationRepository.getAttestation(payload = attestationPayload)

					val response = authApiRepository.refreshTokens(
						accessToken = oldAccessToken,
						refreshToken = oldRefreshToken,
						attestation = attestation
					)

					sessionRepository.setAccessToken(response.accessToken)
					sessionRepository.setRefreshToken(response.refreshToken)

					BearerTokens(
						accessToken = response.accessToken,
						refreshToken = response.refreshToken
					)
				}
			}
		}
	}
}
