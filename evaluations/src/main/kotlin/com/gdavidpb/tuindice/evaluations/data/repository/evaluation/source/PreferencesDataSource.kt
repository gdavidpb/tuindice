package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.SettingsDataSource
import com.gdavidpb.tuindice.evaluations.utils.CooldownTimes
import com.gdavidpb.tuindice.evaluations.utils.PreferencesKeys

class PreferencesDataSource(
	private val sharedPreferences: SharedPreferences
) : SettingsDataSource {
	override suspend fun isGetEvaluationsOnCooldown(): Boolean {
		val cooldownTime = sharedPreferences.getLong(PreferencesKeys.COOLDOWN_GET_EVALUATIONS, 0L)

		return cooldownTime >= System.currentTimeMillis()
	}

	override suspend fun setGetEvaluationsOnCooldown() {
		val cooldownTime = System.currentTimeMillis() + CooldownTimes.COOLDOWN_GET_EVALUATIONS

		sharedPreferences.edit {
			putLong(PreferencesKeys.COOLDOWN_GET_EVALUATIONS, cooldownTime)
		}
	}
}