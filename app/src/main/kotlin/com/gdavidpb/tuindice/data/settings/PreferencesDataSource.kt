package com.gdavidpb.tuindice.data.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.utils.ScreenKeys
import com.gdavidpb.tuindice.base.utils.PreferencesKeys

class PreferencesDataSource(
	private val sharedPreferences: SharedPreferences
) : SettingsRepository {
	override fun getLastScreen(): Int {
		return when (sharedPreferences.getInt(PreferencesKeys.LAST_SCREEN, R.id.fragment_summary)) {
			ScreenKeys.SUMMARY -> R.id.fragment_summary
			ScreenKeys.RECORD -> R.id.fragment_record
			ScreenKeys.ABOUT -> R.id.fragment_about
			else -> R.id.fragment_summary
		}
	}

	override fun setLastScreen(screen: Int) {
		val screenKey = when (screen) {
			R.id.fragment_summary -> ScreenKeys.SUMMARY
			R.id.fragment_record -> ScreenKeys.RECORD
			R.id.fragment_about -> ScreenKeys.ABOUT
			else -> ScreenKeys.SUMMARY
		}

		sharedPreferences.edit {
			putInt(PreferencesKeys.LAST_SCREEN, screenKey)
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

		return counter % value == 0
	}

	override fun clear() {
		sharedPreferences.edit {
			clear()
		}
	}
}