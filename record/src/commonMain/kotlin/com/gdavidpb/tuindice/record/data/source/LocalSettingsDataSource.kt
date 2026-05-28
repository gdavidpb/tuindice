package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.record.data.repository.RecordSettingsDataRepository
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.utils.CooldownTimes
import com.gdavidpb.tuindice.record.utils.PreferencesKeys
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class LocalSettingsDataSource(
	private val settings: Settings
) : RecordSettingsDataRepository {
	private val historicalSelectedTermId = MutableStateFlow(
		settings.getStringOrNull(PreferencesKeys.SELECTED_HISTORICAL_TERM_ID)
	)
	private val projectionSelectedTermId = MutableStateFlow(
		settings.getStringOrNull(PreferencesKeys.SELECTED_PROJECTION_TERM_ID)
	)
	private val recordViewMode = MutableStateFlow(
		RecordViewMode.fromStorageValue(
			settings.getStringOrNull(PreferencesKeys.RECORD_VIEW_MODE)
		)
	)

	override suspend fun isGetAcademicRecordOnCooldown(): Boolean {
		val cooldownTime = settings.getLongOrNull(PreferencesKeys.COOLDOWN_GET_RECORD) ?: 0L

		return cooldownTime >= currentTimeMillis()
	}

	override suspend fun setGetAcademicRecordOnCooldown() {
		val cooldownTime = currentTimeMillis() + CooldownTimes.COOLDOWN_GET_RECORD

		settings.putLong(PreferencesKeys.COOLDOWN_GET_RECORD, cooldownTime)
	}

	override fun observeSelectedTermId(viewMode: RecordViewMode): Flow<String?> {
		return when (viewMode) {
			RecordViewMode.Historical -> historicalSelectedTermId
			RecordViewMode.Projection -> projectionSelectedTermId
		}
	}

	override fun observeRecordViewMode(): Flow<RecordViewMode> {
		return recordViewMode
	}

	override fun getSelectedTermId(viewMode: RecordViewMode): String? {
		return when (viewMode) {
			RecordViewMode.Historical -> historicalSelectedTermId.value
			RecordViewMode.Projection -> projectionSelectedTermId.value
		}
	}

	override fun setSelectedTermId(viewMode: RecordViewMode, termId: String) {
		settings.putString(
			when (viewMode) {
				RecordViewMode.Historical -> PreferencesKeys.SELECTED_HISTORICAL_TERM_ID
				RecordViewMode.Projection -> PreferencesKeys.SELECTED_PROJECTION_TERM_ID
			},
			termId
		)
		when (viewMode) {
			RecordViewMode.Historical -> historicalSelectedTermId.value = termId
			RecordViewMode.Projection -> projectionSelectedTermId.value = termId
		}
	}

	override fun getRecordViewMode(): RecordViewMode {
		return recordViewMode.value
	}

	override fun setRecordViewMode(viewMode: RecordViewMode) {
		settings.putString(
			PreferencesKeys.RECORD_VIEW_MODE,
			viewMode.storageValue
		)
		recordViewMode.value = viewMode
	}
}
