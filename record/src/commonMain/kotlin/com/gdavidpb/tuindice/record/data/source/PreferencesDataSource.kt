package com.gdavidpb.tuindice.record.data.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.record.data.repository.QuarterSettingsDataSource
import com.gdavidpb.tuindice.record.utils.CooldownTimes
import com.gdavidpb.tuindice.record.utils.PreferencesKeys
import kotlinx.coroutines.flow.first

class PreferencesDataSource(
	private val dataStore: DataStore<Preferences>
) : QuarterSettingsDataSource {
	override suspend fun isGetQuartersOnCooldown(): Boolean {
		val cooldownTime = dataStore.data.first()[COOLDOWN_GET_QUARTERS] ?: 0L

		return cooldownTime >= currentTimeMillis()
	}

	override suspend fun setGetQuartersOnCooldown() {
		val cooldownTime = currentTimeMillis() + CooldownTimes.COOLDOWN_GET_QUARTERS

		dataStore.edit { preferences ->
			preferences[COOLDOWN_GET_QUARTERS] = cooldownTime
		}
	}

	companion object {
		private val COOLDOWN_GET_QUARTERS = longPreferencesKey(PreferencesKeys.COOLDOWN_GET_QUARTERS)
	}
}
