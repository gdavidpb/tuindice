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
	private val officialSelectedTermId = MutableStateFlow(
		settings.getStringOrNull(PreferencesKeys.SELECTED_OFFICIAL_TERM_ID)
	)
	private val workingSelectedTermId = MutableStateFlow(
		settings.getStringOrNull(PreferencesKeys.SELECTED_WORKING_TERM_ID)
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
			RecordViewMode.Official -> officialSelectedTermId
			RecordViewMode.Working -> workingSelectedTermId
		}
	}

	override fun observeRecordViewMode(): Flow<RecordViewMode> {
		return recordViewMode
	}

	override fun getSelectedTermId(viewMode: RecordViewMode): String? {
		return when (viewMode) {
			RecordViewMode.Official -> officialSelectedTermId.value
			RecordViewMode.Working -> workingSelectedTermId.value
		}
	}

	override fun setSelectedTermId(viewMode: RecordViewMode, termId: String) {
		settings.putString(
			when (viewMode) {
				RecordViewMode.Official -> PreferencesKeys.SELECTED_OFFICIAL_TERM_ID
				RecordViewMode.Working -> PreferencesKeys.SELECTED_WORKING_TERM_ID
			},
			termId
		)
		when (viewMode) {
			RecordViewMode.Official -> officialSelectedTermId.value = termId
			RecordViewMode.Working -> workingSelectedTermId.value = termId
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
