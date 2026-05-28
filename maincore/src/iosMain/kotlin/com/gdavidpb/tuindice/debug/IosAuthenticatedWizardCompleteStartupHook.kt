package com.gdavidpb.tuindice.debug

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import org.koin.core.Koin

object IosAuthenticatedWizardCompleteStartupHook : IosDebugStartupHook {
	const val NAME = "authenticatedWizardComplete"

	override suspend fun run(koin: Koin, mainSectionName: String) {
		val section = MainSection.valueOf(mainSectionName)
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
}
