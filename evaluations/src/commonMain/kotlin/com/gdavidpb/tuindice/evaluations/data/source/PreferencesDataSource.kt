package com.gdavidpb.tuindice.evaluations.data.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.data.repository.SettingsDataSource
import com.gdavidpb.tuindice.evaluations.utils.CooldownTimes
import com.gdavidpb.tuindice.evaluations.utils.PreferencesKeys
import kotlinx.coroutines.flow.first

class PreferencesDataSource(
	private val dataStore: DataStore<Preferences>
) : SettingsDataSource {
	override suspend fun isGetEvaluationsOnCooldown(): Boolean {
		val cooldownTime = dataStore.data.first()[COOLDOWN_GET_EVALUATIONS] ?: 0L

		return cooldownTime >= currentTimeMillis()
	}

	override suspend fun setGetEvaluationsOnCooldown() {
		val cooldownTime = currentTimeMillis() + CooldownTimes.COOLDOWN_GET_EVALUATIONS

		dataStore.edit { preferences ->
			preferences[COOLDOWN_GET_EVALUATIONS] = cooldownTime
		}
	}

	companion object {
		private val COOLDOWN_GET_EVALUATIONS = longPreferencesKey(PreferencesKeys.COOLDOWN_GET_EVALUATIONS)
	}
}
