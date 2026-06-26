package com.gdavidpb.tuindice.debug

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.wizard.presentation.model.contextualCoachmarks
import com.gdavidpb.tuindice.wizard.presentation.model.persistedId
import org.koin.core.Koin

object IosAuthenticatedCoachmarksSeenStartupHook : IosDebugStartupHook {
	const val NAME = "authenticatedCoachmarksSeen"

	override suspend fun run(koin: Koin, mainSectionName: String) {
		seedAuthenticatedCoachmarkState(
			koin = koin,
			mainSectionName = mainSectionName,
			areCoachmarksSeen = true
		)
	}
}

object IosAuthenticatedCoachmarksPendingStartupHook : IosDebugStartupHook {
	const val NAME = "authenticatedCoachmarksPending"

	override suspend fun run(koin: Koin, mainSectionName: String) {
		seedAuthenticatedCoachmarkState(
			koin = koin,
			mainSectionName = mainSectionName,
			areCoachmarksSeen = false
		)
	}
}

private suspend fun seedAuthenticatedCoachmarkState(
	koin: Koin,
	mainSectionName: String,
	areCoachmarksSeen: Boolean
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
	if (areCoachmarksSeen) {
		contextualCoachmarks().forEach { coachmark ->
			settingsRepository.markCoachmarkSeen(coachmark.id.persistedId)
		}
	}
	settingsRepository.setLastMainSection(section)
}
