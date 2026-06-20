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

	suspend fun isWizardCompleted(): Boolean
	suspend fun setWizardCompleted()

	suspend fun clear()
}
