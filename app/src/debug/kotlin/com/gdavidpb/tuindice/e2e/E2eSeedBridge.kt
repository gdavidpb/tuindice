package com.gdavidpb.tuindice.e2e

import android.content.Intent
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.debug.DebugLaunchArguments
import com.gdavidpb.tuindice.debug.DebugSessionSeed
import com.gdavidpb.tuindice.debug.OverridableNetworkDataSource
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
	/**
	 * Parses the `TUINDICE_E2E_*` extras of the launch intent and applies them.
	 * Returns the parsed arguments so the activity can read the ones it owns.
	 */
	@JvmStatic
	fun applyLaunchArguments(intent: Intent?): DebugLaunchArguments {
		val koin = GlobalContext.get()
		val extras = intent?.extras
		val raw = extras?.keySet().orEmpty()
			.filter { it.startsWith(DebugLaunchArguments.PREFIX) }
			.associateWith { extras?.getString(it).orEmpty() }
		val arguments = DebugLaunchArguments.parse(raw)

		arguments.availabilityNotice?.let { notice ->
			koin.setDebugAppAvailabilityNoticeOverride(
				enabled = notice.enabled,
				title = notice.title,
				message = notice.message
			)
		}

		arguments.networkAvailable?.let { forced ->
			val network = koin.get<NetworkRepository>()

			check(network is OverridableNetworkDataSource) {
				"Network availability overrides require OverridableNetworkDataSource."
			}

			network.forced = forced
		}

		arguments.sessionSeed?.let { seed ->
			runBlocking {
				putWireMockTokensIssuedState(BuildConfig.URL_API)
				seedAuthenticatedState(koin = koin, seed = seed)
			}
		}

		return arguments
	}

	private suspend fun seedAuthenticatedState(
		koin: Koin,
		seed: DebugSessionSeed
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
				sessionId = seed.sessionId,
				accessToken = seed.accessToken,
				refreshToken = seed.refreshToken,
				usbId = seed.usbId
			)
		)
		credentialsRepository.setPassword(seed.password)
		if (seed.coachmarksSeen) {
			contextualCoachmarks().forEach { coachmark ->
				settingsRepository.markCoachmarkSeen(coachmark.id.persistedId)
			}
		}
		settingsRepository.setLastMainSection(seed.mainSection)
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
