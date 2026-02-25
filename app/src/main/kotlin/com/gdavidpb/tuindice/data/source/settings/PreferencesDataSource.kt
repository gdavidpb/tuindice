package com.gdavidpb.tuindice.data.source.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.data.mapper.toDestination
import com.gdavidpb.tuindice.data.mapper.toDestinationName
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import kotlinx.coroutines.flow.first

class PreferencesDataSource(
	private val dataStore: DataStore<Preferences>
) : SettingsRepository {
	override suspend fun getLastDestination(): Destination {
		return dataStore.data.first()[LAST_DESTINATION]
			?.toDestination()
			?: SummaryDestination.NavGraph
	}

	override suspend fun setLastDestination(destination: Destination) {
		dataStore.edit { preferences ->
			preferences[LAST_DESTINATION] = destination.toDestinationName()
		}
	}

	override suspend fun isReviewSuggested(value: Int): Boolean {
		val counter = (dataStore.data.first()[SYNCS_COUNTER] ?: 0) + 1

		dataStore.edit { preferences ->
			preferences[SYNCS_COUNTER] = counter
		}

		return counter == value
	}

	override suspend fun clear() {
		dataStore.edit { preferences ->
			preferences.clear()
		}
	}

	companion object {
		private val LAST_DESTINATION = stringPreferencesKey(PreferencesKeys.LAST_DESTINATION)
		private val SYNCS_COUNTER = intPreferencesKey(PreferencesKeys.SYNCS_COUNTER)
	}
}
