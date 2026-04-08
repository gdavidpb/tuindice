package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.record.data.repository.RecordSettingsDataRepository
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.utils.CooldownTimes
import com.gdavidpb.tuindice.record.utils.PreferencesKeys
import com.russhwolf.settings.Settings

class LocalSettingsDataSource(
	private val settings: Settings
) : RecordSettingsDataRepository {
	override suspend fun isGetAcademicRecordOnCooldown(): Boolean {
		val cooldownTime = settings.getLongOrNull(PreferencesKeys.COOLDOWN_GET_RECORD) ?: 0L

		return cooldownTime >= currentTimeMillis()
	}

	override suspend fun setGetAcademicRecordOnCooldown() {
		val cooldownTime = currentTimeMillis() + CooldownTimes.COOLDOWN_GET_RECORD

		settings.putLong(PreferencesKeys.COOLDOWN_GET_RECORD, cooldownTime)
	}

	override fun getSelectedTermId(viewMode: RecordViewMode): String? {
		return settings.getStringOrNull(
			when (viewMode) {
				RecordViewMode.Official -> PreferencesKeys.SELECTED_OFFICIAL_TERM_ID
				RecordViewMode.Simulation -> PreferencesKeys.SELECTED_SIMULATION_TERM_ID
			}
		)
	}

	override fun setSelectedTermId(viewMode: RecordViewMode, termId: String) {
		settings.putString(
			when (viewMode) {
				RecordViewMode.Official -> PreferencesKeys.SELECTED_OFFICIAL_TERM_ID
				RecordViewMode.Simulation -> PreferencesKeys.SELECTED_SIMULATION_TERM_ID
			},
			termId
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
