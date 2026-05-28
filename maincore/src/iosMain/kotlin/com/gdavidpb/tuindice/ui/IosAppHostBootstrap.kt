package com.gdavidpb.tuindice.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.di.startIosKoin
import com.gdavidpb.tuindice.domain.model.IosAppHostConfig
import com.gdavidpb.tuindice.domain.model.IosBuildVariant
import com.gdavidpb.tuindice.presentation.route.TuIndiceAppHostRoute
import com.gdavidpb.tuindice.ui.theme.TuIndiceSharedTheme
import kotlinx.coroutines.runBlocking
import org.koin.core.Koin
import platform.UIKit.UIViewController

class IosAppHostBootstrap(
	private val hostConfig: IosAppHostConfig
) {
	fun createRootViewController(): UIViewController {
		startIfNeeded()

		return ComposeUIViewController {
			TuIndiceSharedTheme {
				TuIndiceAppHostRoute(
					onConfirmExitClick = {},
					isSwipeBackNavigationEnabled = true
				)
			}
		}
	}

	fun startIfNeeded(): Koin {
		return startIosKoin(hostConfig = hostConfig)
	}

	fun seedE2eState(
		state: String,
		mainSectionName: String
	) {
		check(hostConfig.buildVariant == IosBuildVariant.DEBUG) {
			"E2E seed state is only available in debug iOS builds."
		}
		check(state == AUTHENTICATED_WIZARD_COMPLETE) {
			"Unsupported E2E seed state: $state"
		}

		val section = MainSection.valueOf(mainSectionName)
		val koin = startIfNeeded()
		runBlocking {
			seedAuthenticatedWizardComplete(koin = koin, section = section)
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

	private companion object {
		const val AUTHENTICATED_WIZARD_COMPLETE = "authenticatedWizardComplete"
	}
}
