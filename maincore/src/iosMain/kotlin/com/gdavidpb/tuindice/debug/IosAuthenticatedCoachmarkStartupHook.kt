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
		seedAuthenticatedState(
			koin = koin,
			seed = DebugSessionSeed.Canonical.copy(
				coachmarksSeen = true,
				mainSection = MainSection.valueOf(mainSectionName)
			)
		)
	}
}

object IosAuthenticatedCoachmarksPendingStartupHook : IosDebugStartupHook {
	const val NAME = "authenticatedCoachmarksPending"

	override suspend fun run(koin: Koin, mainSectionName: String) {
		seedAuthenticatedState(
			koin = koin,
			seed = DebugSessionSeed.Canonical.copy(
				coachmarksSeen = false,
				mainSection = MainSection.valueOf(mainSectionName)
			)
		)
	}
}

internal suspend fun seedAuthenticatedState(
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
