package com.gdavidpb.tuindice.data.ios

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gdavidpb.tuindice.base.data.source.SecureStoreDataSource
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.di.IosExternalActionsCapability
import com.gdavidpb.tuindice.di.temporaryStorageRoot
import com.gdavidpb.tuindice.presentation.navigation.toDestinationOrDefault
import com.gdavidpb.tuindice.presentation.navigation.toPersistedName
import kotlinx.coroutines.flow.first
import okio.FileSystem

internal class IosSettingsDataSource(
	private val dataStore: DataStore<Preferences>
) : SettingsRepository {
	override suspend fun isReviewSuggested(value: Int): Boolean {
		val counter = (dataStore.data.first()[SYNCS_COUNTER] ?: 0) + 1

		dataStore.edit { preferences ->
			preferences[SYNCS_COUNTER] = counter
		}

		return counter == value
	}

	override suspend fun getLastDestination(): Destination {
		return dataStore.data.first()[LAST_DESTINATION]
			.toDestinationOrDefault()
	}

	override suspend fun setLastDestination(destination: Destination) {
		dataStore.edit { preferences ->
			preferences[LAST_DESTINATION] = destination.toPersistedName()
		}
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

internal class IosApplicationDataSource(
	private val dataStore: DataStore<Preferences>,
	private val secureStoreDataSource: SecureStoreDataSource,
	private val externalActionsCapability: IosExternalActionsCapability
) : ApplicationRepository {
	override suspend fun createTemporaryFile(nameHint: String): PlatformFileRef {
		val storagePath = temporaryStorageRoot().resolve(nameHint)

		FileSystem.SYSTEM.createDirectories(storagePath.parent!!)
		FileSystem.SYSTEM.delete(storagePath, mustExist = false)
		FileSystem.SYSTEM.write(storagePath) {}

		return PlatformFileRef(storagePath.toString())
	}

	override suspend fun canOpen(fileRef: PlatformFileRef): Boolean {
		return externalActionsCapability.canOpen(fileRef)
	}

	override suspend fun clearData() {
		dataStore.edit { preferences ->
			preferences.clear()
		}

		secureStoreDataSource.clear()

		runCatching {
			FileSystem.SYSTEM.deleteRecursively(temporaryStorageRoot(), mustExist = false)
		}
	}
}
