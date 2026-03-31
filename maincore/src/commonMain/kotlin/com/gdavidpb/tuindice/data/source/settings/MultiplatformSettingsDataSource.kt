package com.gdavidpb.tuindice.data.source.settings

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.data.mapper.toMainSectionOrThrow
import com.gdavidpb.tuindice.data.mapper.toPersistedName
import com.russhwolf.settings.Settings

class MultiplatformSettingsDataSource(
	private val settings: Settings
) : SettingsRepository {
	override suspend fun getLastMainSection(): MainSection {
		return settings.getStringOrNull(LAST_MAIN_SECTION_KEY)
			?.toMainSectionOrThrow()
			?: MainSection.SUMMARY
	}

	override suspend fun setLastMainSection(section: MainSection) {
		settings.putString(
			key = LAST_MAIN_SECTION_KEY,
			value = section.toPersistedName()
		)
	}

	override suspend fun isReviewSuggested(value: Int): Boolean {
		val counter = (settings.getIntOrNull(PreferencesKeys.SYNCS_COUNTER) ?: 0) + 1

		settings.putInt(PreferencesKeys.SYNCS_COUNTER, counter)

		return counter == value
	}

	override suspend fun clear() {
		settings.clear()
	}
}

private const val LAST_MAIN_SECTION_KEY = "lastDestination"
