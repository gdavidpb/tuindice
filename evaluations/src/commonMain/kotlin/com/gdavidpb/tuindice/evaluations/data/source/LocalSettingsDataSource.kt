package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.data.repository.SettingsDataRepository
import com.gdavidpb.tuindice.evaluations.domain.model.SelectedWeekPick
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationsSelectionRepository
import com.gdavidpb.tuindice.evaluations.utils.CooldownTimes
import com.gdavidpb.tuindice.evaluations.utils.PreferencesKeys
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow

class LocalSettingsDataSource(
	private val settings: Settings,
	private val selectedWeekWriter: SelectedWeekWriter
) : SettingsDataRepository, EvaluationsSelectionRepository {
	override suspend fun isGetEvaluationsOnCooldown(): Boolean {
		val cooldownTime = settings.getLongOrNull(PreferencesKeys.COOLDOWN_GET_EVALUATIONS) ?: 0L

		return cooldownTime >= currentTimeMillis()
	}

	override suspend fun setGetEvaluationsOnCooldown() {
		val cooldownTime = currentTimeMillis() + CooldownTimes.COOLDOWN_GET_EVALUATIONS

		settings.putLong(PreferencesKeys.COOLDOWN_GET_EVALUATIONS, cooldownTime)
	}

	override fun observeSelectedWeekKey(): Flow<String?> {
		return selectedWeekWriter.observeSelectedWeekKey()
	}

	override suspend fun saveSelectedWeekPick(pick: SelectedWeekPick) {
		selectedWeekWriter.save(pick = pick)
	}
}
