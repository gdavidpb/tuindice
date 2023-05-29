package com.gdavidpb.tuindice.data.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.utils.PreferencesKeys

class PreferencesDataSource(
	private val sharedPreferences: SharedPreferences
) : SettingsRepository {
	override fun getLastScreen(): String {
		return sharedPreferences
			.getString(PreferencesKeys.LAST_SCREEN, null)
			?: Destination.Summary.route
	}

	override fun setLastScreen(route: String) {
		sharedPreferences.edit {
			putString(PreferencesKeys.LAST_SCREEN, route)
		}
	}

	override fun getActiveToken(): String? {
		return sharedPreferences.getString(PreferencesKeys.ACTIVE_TOKEN, null)
	}

	override fun setActiveToken(token: String) {
		sharedPreferences.edit {
			putString(PreferencesKeys.ACTIVE_TOKEN, token)
		}
	}

	override fun isReviewSuggested(value: Int): Boolean {
		val counter = sharedPreferences.getInt(PreferencesKeys.SYNCS_COUNTER, 0) + 1

		sharedPreferences.edit {
			putInt(PreferencesKeys.SYNCS_COUNTER, counter)
		}

		return counter == value
	}

	override fun clear() {
		sharedPreferences.edit {
			clear()
		}
	}
}