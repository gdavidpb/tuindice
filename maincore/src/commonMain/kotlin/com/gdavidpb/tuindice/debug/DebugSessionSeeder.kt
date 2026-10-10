package com.gdavidpb.tuindice.debug

import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.wizard.presentation.model.contextualCoachmarks
import com.gdavidpb.tuindice.wizard.presentation.model.persistedId

/**
 * Leaves the app as a real sign-in does, minus the network round trip: the previous session,
 * settings, password and sync status are cleared first, then the snapshot, the password, the
 * local data owner, the coachmarks and the last main section are stored.
 *
 * It does not wipe the database (the driver starts from cleared app state) and it does not
 * schedule a sync: the main route asks for one whenever the content is available.
 */
class DebugSessionSeeder(
	private val sessionRepository: SessionRepository,
	private val settingsRepository: SettingsRepository,
	private val credentialsRepository: CredentialsRepository,
	private val syncStatusRepository: SyncStatusRepository
) {
	suspend fun seed(seed: DebugSessionSeed) {
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
		settingsRepository.setLocalDataOwner(seed.usbId)

		if (seed.coachmarksSeen) {
			contextualCoachmarks().forEach { coachmark ->
				settingsRepository.markCoachmarkSeen(coachmark.id.persistedId)
			}
		}

		settingsRepository.setLastMainSection(seed.mainSection)
	}
}
