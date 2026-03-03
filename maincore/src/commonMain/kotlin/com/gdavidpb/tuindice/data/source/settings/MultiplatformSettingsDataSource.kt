package com.gdavidpb.tuindice.data.source.settings

import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.presentation.navigation.toDestinationOrThrow
import com.gdavidpb.tuindice.presentation.navigation.toPersistedName
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.russhwolf.settings.Settings

class MultiplatformSettingsDataSource(
	private val settings: Settings
) : SettingsRepository {
	override suspend fun getLastDestination(): Destination {
		return settings.getStringOrNull(PreferencesKeys.LAST_DESTINATION)
			?.toDestinationOrThrow()
			?: SummaryDestination.NavGraph
	}

	override suspend fun setLastDestination(destination: Destination) {
		settings.putString(
			key = PreferencesKeys.LAST_DESTINATION,
			value = destination.toPersistedName()
		)
	}

	override suspend fun isReviewSuggested(value: Int): Boolean {
		val counter = (settings.getIntOrNull(PreferencesKeys.SYNCS_COUNTER) ?: 0) + 1

		settings.putInt(PreferencesKeys.SYNCS_COUNTER, counter)

		return counter == value
	}

	override suspend fun clear() {
		settings.clear()
	}
}
