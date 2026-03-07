package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.network.createPlatformHttpClient
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.model.RiskAttestationRequest
import com.gdavidpb.tuindice.base.domain.repository.RiskAttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.utils.canonicalRiskPayloadJson
import com.gdavidpb.tuindice.login.domain.model.RefreshTokensRiskPayload
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository
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
	riskAttestationRepositoryProvider: () -> RiskAttestationRepository,
	loginRepositoryProvider: () -> LoginRepository,
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
					header == "X-Risk-Attestation"
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
					val riskAttestationRepository = riskAttestationRepositoryProvider()
					val loginRepository = loginRepositoryProvider()

					val riskPayload = RefreshTokensRiskPayload(
						accessToken = oldAccessToken,
						refreshToken = oldRefreshToken
					)

					val riskAttestation = riskAttestationRepository.issueProof(
						request = RiskAttestationRequest(
							operation = com.gdavidpb.tuindice.base.domain.model.RiskOperation.RefreshTokens,
							payloadJson = canonicalRiskPayloadJson(
								serializer = RefreshTokensRiskPayload.serializer(),
								value = riskPayload
							)
						)
					)

					val response = loginRepository.refreshTokens(
						accessToken = oldAccessToken,
						refreshToken = oldRefreshToken,
						riskAttestation = riskAttestation
					)

					BearerTokens(
						accessToken = response.accessToken,
						refreshToken = response.refreshToken
					)
				}
			}
		}
	}
}
