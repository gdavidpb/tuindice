package com.gdavidpb.tuindice.data.source.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.data.mapper.toDestination
import com.gdavidpb.tuindice.data.mapper.toDestinationName
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination

class PreferencesDataSource(
	private val sharedPreferences: SharedPreferences
) : SettingsRepository {
	override suspend fun getLastDestination(): Destination {
		return sharedPreferences
			.getString(PreferencesKeys.LAST_DESTINATION, null)
			?.toDestination()
			?: SummaryDestination.NavGraph
	}

	override suspend fun setLastDestination(destination: Destination) {
		sharedPreferences.edit {
			putString(PreferencesKeys.LAST_DESTINATION, destination.toDestinationName())
		}
	}

	override suspend fun isReviewSuggested(value: Int): Boolean {
		val counter = sharedPreferences.getInt(PreferencesKeys.SYNCS_COUNTER, 0) + 1

		sharedPreferences.edit {
			putInt(PreferencesKeys.SYNCS_COUNTER, counter)
		}

		return counter == value
	}

	override suspend fun clear() {
		sharedPreferences.edit {
			clear()
		}
	}
}