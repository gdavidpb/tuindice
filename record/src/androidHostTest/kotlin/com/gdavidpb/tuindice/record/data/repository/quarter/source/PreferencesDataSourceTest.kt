package com.gdavidpb.tuindice.record.data.repository.quarter.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.gdavidpb.tuindice.record.utils.CooldownTimes
import com.gdavidpb.tuindice.record.utils.PreferencesKeys
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PreferencesDataSourceTest {
	@Test
	fun isGetQuartersOnCooldown_whenStoredTimestampIsFuture_returnsTrue() = runBlocking {
		val dataStore = createDataStore()
		val dataSource = PreferencesDataSource(dataStore)

		dataStore.edit { preferences ->
			preferences[COOLDOWN_GET_QUARTERS] = System.currentTimeMillis() + 10_000L
		}

		val result = dataSource.isGetQuartersOnCooldown()

		assertTrue(result)
	}

	@Test
	fun isGetQuartersOnCooldown_whenStoredTimestampIsPast_returnsFalse() = runBlocking {
		val dataStore = createDataStore()
		val dataSource = PreferencesDataSource(dataStore)

		dataStore.edit { preferences ->
			preferences[COOLDOWN_GET_QUARTERS] = System.currentTimeMillis() - 10_000L
		}

		val result = dataSource.isGetQuartersOnCooldown()

		assertFalse(result)
	}

	@Test
	fun setGetQuartersOnCooldown_storesFutureTimestamp() = runBlocking {
		val dataStore = createDataStore()
		val dataSource = PreferencesDataSource(dataStore)

		val before = System.currentTimeMillis()
		dataSource.setGetQuartersOnCooldown()
		val after = System.currentTimeMillis()

		val storedValue = dataStore.data.first()[COOLDOWN_GET_QUARTERS] ?: 0L

		assertTrue(storedValue >= before + CooldownTimes.COOLDOWN_GET_QUARTERS)
		assertTrue(storedValue <= after + CooldownTimes.COOLDOWN_GET_QUARTERS + 250L)
	}

	private fun createDataStore(): DataStore<Preferences> {
		val file = File.createTempFile("record-preferences-", ".preferences_pb").apply {
			deleteOnExit()
		}

		return PreferenceDataStoreFactory.create(
			produceFile = { file }
		)
	}

	companion object {
		private val COOLDOWN_GET_QUARTERS = longPreferencesKey(PreferencesKeys.COOLDOWN_GET_QUARTERS)
	}
}
