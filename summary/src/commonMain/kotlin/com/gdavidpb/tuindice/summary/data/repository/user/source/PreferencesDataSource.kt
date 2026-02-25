package com.gdavidpb.tuindice.summary.data.repository.user.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.summary.data.repository.user.SettingsDataSource
import com.gdavidpb.tuindice.summary.utils.CooldownTimes
import com.gdavidpb.tuindice.summary.utils.PreferencesKeys
import kotlinx.coroutines.flow.first

class PreferencesDataSource(
	private val dataStore: DataStore<Preferences>
) : SettingsDataSource {
	override suspend fun isGetUserOnCooldown(): Boolean {
		val cooldownTime = dataStore.data.first()[COOLDOWN_GET_USER] ?: 0L

		return cooldownTime >= currentTimeMillis()
	}

	override suspend fun setGetUserOnCooldown() {
		val cooldownTime = currentTimeMillis() + CooldownTimes.COOLDOWN_GET_USER

		dataStore.edit { preferences ->
			preferences[COOLDOWN_GET_USER] = cooldownTime
		}
	}

	companion object {
		private val COOLDOWN_GET_USER = longPreferencesKey(PreferencesKeys.COOLDOWN_GET_USER)
	}
}
