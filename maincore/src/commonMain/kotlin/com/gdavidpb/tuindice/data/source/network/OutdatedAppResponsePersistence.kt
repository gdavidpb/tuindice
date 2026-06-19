package com.gdavidpb.tuindice.data.source.network

import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.repository.OutdatedAppEventRepository
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

internal fun HttpClientConfig<*>.installOutdatedAppPersistence(
	settingsRepository: SettingsRepository,
	outdatedAppEventRepository: OutdatedAppEventRepository,
	userAgentValue: String?
) {
	HttpResponseValidator {
		handleResponseExceptionWithRequest { exception, _ ->
			exception.persistOutdatedAppStateIfUpgradeRequired(
				settingsRepository = settingsRepository,
				outdatedAppEventRepository = outdatedAppEventRepository,
				userAgentValue = userAgentValue
			)
		}
	}
}

internal suspend fun Throwable.persistOutdatedAppStateIfUpgradeRequired(
	settingsRepository: SettingsRepository,
	outdatedAppEventRepository: OutdatedAppEventRepository,
	userAgentValue: String?
) {
	val clientRequestException = this as? ClientRequestException ?: return
	if (clientRequestException.response.status != HttpStatusCode.UpgradeRequired) return

	clientRequestException.response.persistOutdatedAppState(
		settingsRepository = settingsRepository,
		outdatedAppEventRepository = outdatedAppEventRepository,
		userAgentValue = userAgentValue
	)
}

private suspend fun HttpResponse.persistOutdatedAppState(
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
