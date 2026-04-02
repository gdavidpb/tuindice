package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.record.data.repository.QuarterSettingsDataRepository
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.utils.CooldownTimes
import com.gdavidpb.tuindice.record.utils.PreferencesKeys
import com.russhwolf.settings.Settings

class LocalSettingsDataSource(
	private val settings: Settings
) : QuarterSettingsDataRepository {
	override suspend fun isGetQuartersOnCooldown(): Boolean {
		val cooldownTime = settings.getLongOrNull(PreferencesKeys.COOLDOWN_GET_QUARTERS) ?: 0L

		return cooldownTime >= currentTimeMillis()
	}

	override suspend fun setGetQuartersOnCooldown() {
		val cooldownTime = currentTimeMillis() + CooldownTimes.COOLDOWN_GET_QUARTERS

		settings.putLong(PreferencesKeys.COOLDOWN_GET_QUARTERS, cooldownTime)
	}

	override fun getSelectedQuarterId(viewMode: RecordViewMode): String? {
		return settings.getStringOrNull(
			when (viewMode) {
				RecordViewMode.Official -> PreferencesKeys.SELECTED_OFFICIAL_QUARTER_ID
				RecordViewMode.Simulation -> PreferencesKeys.SELECTED_SIMULATION_QUARTER_ID
			}
		)
	}

	override fun setSelectedQuarterId(viewMode: RecordViewMode, quarterId: String) {
		settings.putString(
			when (viewMode) {
				RecordViewMode.Official -> PreferencesKeys.SELECTED_OFFICIAL_QUARTER_ID
				RecordViewMode.Simulation -> PreferencesKeys.SELECTED_SIMULATION_QUARTER_ID
			},
			quarterId
		)
	}

	override fun getRecordViewMode(): RecordViewMode {
		return RecordViewMode.fromStorageValue(
			settings.getStringOrNull(PreferencesKeys.RECORD_VIEW_MODE)
		)
	}

	override fun setRecordViewMode(viewMode: RecordViewMode) {
		settings.putString(
			PreferencesKeys.RECORD_VIEW_MODE,
			viewMode.storageValue
		)
	}
}
