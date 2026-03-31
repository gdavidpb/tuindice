package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.MainSection

interface SettingsRepository {
	suspend fun isReviewSuggested(value: Int): Boolean

	suspend fun getLastMainSection(): MainSection
	suspend fun setLastMainSection(section: MainSection)

	suspend fun clear()
}
