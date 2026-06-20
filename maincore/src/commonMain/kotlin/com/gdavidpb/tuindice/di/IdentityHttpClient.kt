package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.network.AttestationHeaders
import com.gdavidpb.tuindice.base.data.source.network.createPlatformHttpClient
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.data.source.network.installOutdatedAppPersistence
import com.gdavidpb.tuindice.domain.repository.OutdatedAppEventRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

const val IDENTITY_HTTP_CLIENT_QUALIFIER = "identityHttpClient"

fun createIdentityHttpClient(
	appEnvironmentRepository: AppEnvironmentRepository,
	configRepository: ConfigRepository,
	settingsRepository: SettingsRepository,
	outdatedAppEventRepository: OutdatedAppEventRepository,
	logger: Logger,
	json: Json,
	userAgentValue: String?
): HttpClient {
	return createPlatformHttpClient {
		expectSuccess = true

		install(DefaultRequest) {
			val appEnvironment = appEnvironmentRepository.getEnvironment()

			url(appEnvironment.apiBaseUrl)
			contentType(ContentType.Application.Json)

			if (!userAgentValue.isNullOrBlank()) {
				header(HttpHeaders.UserAgent, userAgentValue)
			}
		}

		install(HttpTimeout) {
			val timeout = configRepository.getTimeout()

			requestTimeoutMillis = timeout
			connectTimeoutMillis = timeout
			socketTimeoutMillis = timeout
		}

		installOutdatedAppPersistence(
			settingsRepository = settingsRepository,
			outdatedAppEventRepository = outdatedAppEventRepository,
			userAgentValue = userAgentValue
		)

		install(ContentNegotiation) {
			json(json)
		}

		install(Logging) {
			this.logger = logger
			level = LogLevel.ALL

			sanitizeHeader { header ->
				header == HttpHeaders.Authorization ||
						header == AttestationHeaders.ATTESTATION_TOKEN
			}
		}
	}
}
