package com.gdavidpb.tuindice.debug

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import org.koin.core.Koin

object IosAuthenticatedWizardCompleteStartupHook : IosDebugStartupHook {
	const val NAME = "authenticatedWizardComplete"

	override suspend fun run(koin: Koin, mainSectionName: String) {
		seedAuthenticatedWizardState(
			koin = koin,
			mainSectionName = mainSectionName,
			isWizardCompleted = true
		)
	}
}

object IosAuthenticatedWizardPendingStartupHook : IosDebugStartupHook {
	const val NAME = "authenticatedWizardPending"

	override suspend fun run(koin: Koin, mainSectionName: String) {
		seedAuthenticatedWizardState(
			koin = koin,
			mainSectionName = mainSectionName,
			isWizardCompleted = false
		)
	}
}

private suspend fun seedAuthenticatedWizardState(
	koin: Koin,
	mainSectionName: String,
	isWizardCompleted: Boolean
) {
	val section = MainSection.valueOf(mainSectionName)
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
	if (isWizardCompleted) {
		settingsRepository.setWizardCompleted()
	}
	settingsRepository.setLastMainSection(section)
}
