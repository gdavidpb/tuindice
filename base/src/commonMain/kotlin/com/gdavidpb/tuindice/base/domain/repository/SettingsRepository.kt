package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState

interface SettingsRepository {
	suspend fun isReviewSuggested(value: Int): Boolean

	suspend fun getLastMainSection(): MainSection
	suspend fun setLastMainSection(section: MainSection)

	suspend fun getOutdatedAppState(): OutdatedAppState?
	suspend fun setOutdatedAppState(state: OutdatedAppState)
	suspend fun clearOutdatedAppState()

	suspend fun migrateLegacyOnboardingState(completedCoachmarkIds: Set<String>)
	suspend fun getSeenCoachmarkIds(): Set<String>
	suspend fun markCoachmarkSeen(coachmarkId: String)

	suspend fun setSessionResetNoticePending()
	suspend fun consumeSessionResetNoticePending(): Boolean

	// Identidad dueña de los datos locales. Se escribe al final del alta de sesión y
	// se borra con el resto de los settings, que `clearData()` limpia DESPUÉS de la
	// base: si el borrado de datos falla, la marca sobrevive y el desajuste es
	// detectable en el siguiente inicio de sesión.
	suspend fun getLocalDataOwner(): String?
	suspend fun setLocalDataOwner(usbId: String)

	suspend fun clear()
}
