package com.gdavidpb.tuindice.data.source.settings

import android.content.SharedPreferences
import com.gdavidpb.tuindice.data.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.data.utils.KEY_FIRST_RUN
import com.gdavidpb.tuindice.data.utils.KEY_REFRESH_COOLDOWN
import com.gdavidpb.tuindice.data.utils.edit
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import java.util.*

open class PreferencesDataStore(
        private val preferences: SharedPreferences
) : SettingsRepository {
    override fun clearPrivacyPolicyUrl() {
        preferences.edit {
            remove("privacyPolicy")
        }
    }

    override fun getPrivacyPolicyUrl(): String {
        return preferences.getString("privacyPolicy", null) ?: ""
    }

    override fun setPrivacyPolicyUrl(url: String) {
        preferences.edit {
            putString("privacyPolicy", url)
        }
    }

    override fun setCooldown() {
        val calendar = Calendar.getInstance(DEFAULT_LOCALE)

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        calendar.add(Calendar.DATE, 1)

        preferences.edit {
            putLong(KEY_REFRESH_COOLDOWN, calendar.timeInMillis)
        }
    }

    override fun isCooldown(): Boolean {
        val now = Calendar.getInstance(DEFAULT_LOCALE)
        val cooldown = Calendar.getInstance(DEFAULT_LOCALE)

        cooldown.timeInMillis = preferences.getLong(KEY_REFRESH_COOLDOWN, now.timeInMillis)

        return now.before(cooldown)
    }

    override fun isFirstRun(): Boolean {
        return preferences.getBoolean(KEY_FIRST_RUN, true)
    }

    override fun setFirstRun() {
        preferences.edit {
            putBoolean(KEY_FIRST_RUN, true)
        }
    }
}