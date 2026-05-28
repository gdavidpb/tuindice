package com.gdavidpb.tuindice.e2e

import android.content.Intent
import androidx.activity.ComponentActivity
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.runBlocking
import org.koin.core.Koin
import org.koin.core.context.GlobalContext

object E2eSeedBridge {
	private const val SEED_STATE_ARG = "TUINDICE_E2E_SEED_STATE"
	private const val MAIN_SECTION_ARG = "TUINDICE_E2E_MAIN_SECTION"
	private const val AUTHENTICATED_WIZARD_COMPLETE = "authenticatedWizardComplete"

	@JvmStatic
	fun seedIfRequested(activity: ComponentActivity, intent: Intent?) {
		val seedState = intent?.getStringExtra(SEED_STATE_ARG)
			?.takeIf { it.isNotBlank() }
			?: return

		check(seedState == AUTHENTICATED_WIZARD_COMPLETE) {
			"Unsupported E2E seed state: $seedState"
		}

		val section = intent.getStringExtra(MAIN_SECTION_ARG)
			?.takeIf { it.isNotBlank() }
			?.let(MainSection::valueOf)
			?: MainSection.SUMMARY

		runBlocking {
			putWireMockTokensIssuedState(BuildConfig.URL_API)
			seedAuthenticatedWizardComplete(
				koin = GlobalContext.get(),
				section = section
			)
		}
	}

	private suspend fun seedAuthenticatedWizardComplete(
		koin: Koin,
		section: MainSection
	) {
		val sessionRepository = koin.get<SessionRepository>()
		val settingsRepository = koin.get<SettingsRepository>()
		val credentialsRepository = koin.get<CredentialsRepository>()
		val syncStatusRepository = koin.get<SyncStatusRepository>()

		sessionRepository.clear()
		settingsRepository.clear()
		credentialsRepository.clearPassword()
		syncStatusRepository.reset()

		sessionRepository.setUsbId("11-11111")
		sessionRepository.setSessionId("auth-session-initial")
		sessionRepository.setAccessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.exchange.mock.access")
		sessionRepository.setRefreshToken("refresh.mock.token.value")
		credentialsRepository.setPassword("123456")
		settingsRepository.setWizardCompleted()
		settingsRepository.setLastMainSection(section)
	}

	private fun putWireMockTokensIssuedState(apiBaseUrl: String) {
		val adminUrl = apiBaseUrl.trimEnd('/') +
			"/__admin/scenarios/login-token-lifecycle/state"
		val connection = (URL(adminUrl).openConnection() as HttpURLConnection).apply {
			requestMethod = "PUT"
			connectTimeout = 3000
			readTimeout = 3000
			doOutput = true
			setRequestProperty("Content-Type", "application/json")
		}

		try {
			connection.outputStream.use { outputStream ->
				outputStream.write("""{"state":"TokensIssued"}""".toByteArray())
			}
			check(connection.responseCode in 200..299) {
				"WireMock scenario seed failed with HTTP ${connection.responseCode}"
			}
		} finally {
			connection.disconnect()
		}
	}
}
