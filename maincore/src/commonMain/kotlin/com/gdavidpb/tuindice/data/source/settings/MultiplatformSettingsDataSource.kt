package com.gdavidpb.tuindice.data.source.settings

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
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

	override suspend fun getOutdatedAppState(): OutdatedAppState? {
		return settings.getLongOrNull(OUTDATED_APP_MIN_VERSION_CODE_KEY)
			?.let { minimumVersionCode -> OutdatedAppState(minimumVersionCode = minimumVersionCode) }
	}

	override suspend fun setOutdatedAppState(state: OutdatedAppState) {
		settings.putLong(OUTDATED_APP_MIN_VERSION_CODE_KEY, state.minimumVersionCode)
	}

	override suspend fun clearOutdatedAppState() {
		settings.remove(OUTDATED_APP_MIN_VERSION_CODE_KEY)
	}

	override suspend fun isReviewSuggested(value: Int): Boolean {
		val counter = (settings.getIntOrNull(PreferencesKeys.SYNCS_COUNTER) ?: 0) + 1

		settings.putInt(PreferencesKeys.SYNCS_COUNTER, counter)

		return counter == value
	}

	override suspend fun isWizardCompleted(): Boolean {
		return settings.getBooleanOrNull(WIZARD_COMPLETED_KEY)
			?: settings.getBooleanOrNull(LEGACY_GUIDED_TOUR_COMPLETED_KEY)
			?: false
	}

	override suspend fun setWizardCompleted() {
		settings.putBoolean(WIZARD_COMPLETED_KEY, true)
	}

	override suspend fun clear() {
		settings.clear()
	}
}

private const val LAST_MAIN_SECTION_KEY = "lastDestination"
private const val OUTDATED_APP_MIN_VERSION_CODE_KEY = "outdatedAppMinVersionCode"
private const val WIZARD_COMPLETED_KEY = "wizardCompleted"
private const val LEGACY_GUIDED_TOUR_COMPLETED_KEY = "guidedTourCompleted"
