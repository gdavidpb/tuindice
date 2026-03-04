package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.network.createPlatformHttpClient
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

const val IOS_IDENTITY_HTTP_CLIENT_QUALIFIER = "iosIdentityHttpClient"

fun createIosUserAgent(deviceCapability: IosDeviceCapability): String {
	val appVersionName = deviceCapability.appVersionName().ifBlank { "0.0.0" }
	val appVersionCode = deviceCapability.appVersionCode().coerceAtLeast(0L)
	val device = UIDevice.currentDevice
	val osVersion = device.systemVersion.ifBlank { "Unknown" }
	val osCode = osVersion
		.substringBefore(".")
		.toIntOrNull()
		?: 0
	val osId = NSBundle.mainBundle.objectForInfoDictionaryKey("DTPlatformBuild")
		?.toString()
		?.takeIf { it.isNotBlank() }
		?: "Unknown"
	val model = device.model.takeIf { it.isNotBlank() } ?: "Unknown"

	return buildStructuredUserAgent(
		appVersionName = appVersionName,
		appVersionCode = appVersionCode,
		osName = "iOS",
		osVersion = osVersion,
		osCode = osCode,
		osId = osId,
		manufacturer = "Apple",
		model = model
	)
}

fun createIosIdentityHttpClient(
	appEnvironmentRepository: AppEnvironmentRepository,
	configRepository: ConfigRepository,
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
	}
}