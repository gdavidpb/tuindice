package com.gdavidpb.tuindice.data.repository.messaging.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.data.repository.messaging.LocalDataSource
import kotlinx.coroutines.flow.first

class MessagingPreferencesDataSource(
	private val dataStore: DataStore<Preferences>
) : LocalDataSource {
	override suspend fun isSubscribed(): Boolean {
		return dataStore.data.first()[IS_SUBSCRIBED] ?: false
	}

	override suspend fun getSubscribedToken(): String? {
		return dataStore.data.first()[PUSH_TOKEN]
	}

	override suspend fun markAsSubscribed(token: String) {
		dataStore.edit { preferences ->
			preferences[IS_SUBSCRIBED] = true
			preferences[PUSH_TOKEN] = token
		}
	}

	override suspend fun clearSubscription() {
		dataStore.edit { preferences ->
			preferences.remove(IS_SUBSCRIBED)
			preferences.remove(PUSH_TOKEN)
		}
	}

	companion object {
		private val IS_SUBSCRIBED = booleanPreferencesKey(PreferencesKeys.IS_SUBSCRIBED)
		private val PUSH_TOKEN = stringPreferencesKey(PreferencesKeys.PUSH_TOKEN)
	}
}
