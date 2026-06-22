package com.gdavidpb.tuindice.e2e

import android.content.Intent
import androidx.activity.ComponentActivity
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.debug.setDebugAppAvailabilityNoticeOverride
import com.gdavidpb.tuindice.wizard.presentation.model.contextualCoachmarks
import com.gdavidpb.tuindice.wizard.presentation.model.persistedId
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.Koin
import org.koin.core.context.GlobalContext

object E2eSeedBridge {
	private const val SEED_STATE_ARG = "TUINDICE_E2E_SEED_STATE"
	private const val MAIN_SECTION_ARG = "TUINDICE_E2E_MAIN_SECTION"
	private const val AVAILABILITY_NOTICE_ENABLED_ARG = "TUINDICE_E2E_AVAILABILITY_NOTICE_ENABLED"
	private const val AVAILABILITY_NOTICE_TITLE_ARG = "TUINDICE_E2E_AVAILABILITY_NOTICE_TITLE"
	private const val AVAILABILITY_NOTICE_MESSAGE_ARG = "TUINDICE_E2E_AVAILABILITY_NOTICE_MESSAGE"
	private const val AUTHENTICATED_COACHMARKS_SEEN = "authenticatedCoachmarksSeen"
	private const val AUTHENTICATED_COACHMARKS_PENDING = "authenticatedCoachmarksPending"

	@JvmStatic
	fun seedIfRequested(activity: ComponentActivity, intent: Intent?) {
		configureAvailabilityNoticeOverrideIfRequested(
			koin = GlobalContext.get(),
			intent = intent
		)

		val seedState = intent?.getStringExtra(SEED_STATE_ARG)
			?.takeIf { it.isNotBlank() }
			?: return

		check(seedState in setOf(AUTHENTICATED_COACHMARKS_SEEN, AUTHENTICATED_COACHMARKS_PENDING)) {
			"Unsupported E2E seed state: $seedState"
		}

		val section = intent.getStringExtra(MAIN_SECTION_ARG)
			?.takeIf { it.isNotBlank() }
			?.let(MainSection::valueOf)
			?: MainSection.SUMMARY

		runBlocking {
			putWireMockTokensIssuedState(BuildConfig.URL_API)
			seedAuthenticatedCoachmarkState(
				koin = GlobalContext.get(),
				section = section,
				areCoachmarksSeen = seedState == AUTHENTICATED_COACHMARKS_SEEN
			)
		}
	}

	private fun configureAvailabilityNoticeOverrideIfRequested(koin: Koin, intent: Intent?) {
		val enabled = intent?.getStringExtra(AVAILABILITY_NOTICE_ENABLED_ARG)
			?.takeIf { it.isNotBlank() }
			?.toBooleanStrictOrNull()
			?: return

		koin.setDebugAppAvailabilityNoticeOverride(
			enabled = enabled,
			title = intent.getStringExtra(AVAILABILITY_NOTICE_TITLE_ARG).orEmpty(),
			message = intent.getStringExtra(AVAILABILITY_NOTICE_MESSAGE_ARG).orEmpty()
		)
	}

	private suspend fun seedAuthenticatedCoachmarkState(
		koin: Koin,
		section: MainSection,
		areCoachmarksSeen: Boolean
	) {
		val sessionRepository = koin.get<SessionRepository>()
		val settingsRepository = koin.get<SettingsRepository>()
		val credentialsRepository = koin.get<CredentialsRepository>()
		val syncStatusRepository = koin.get<SyncStatusRepository>()

		sessionRepository.clear()
		settingsRepository.clear()
		credentialsRepository.clearPassword()
		syncStatusRepository.reset()

		sessionRepository.setSessionSnapshot(
			SessionSnapshot(
				sessionId = "auth-session-initial",
				accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.exchange.mock.access",
				refreshToken = "refresh.mock.token.value",
				usbId = "11-11111"
			)
		)
		credentialsRepository.setPassword("123456")
		if (areCoachmarksSeen) {
			contextualCoachmarks().forEach { coachmark ->
				settingsRepository.markCoachmarkSeen(coachmark.id.persistedId)
			}
		}
		settingsRepository.setLastMainSection(section)
	}

	private suspend fun putWireMockTokensIssuedState(apiBaseUrl: String) = withContext(Dispatchers.IO) {
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
