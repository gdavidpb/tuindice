package com.gdavidpb.tuindice.data.source.settings

import android.content.SharedPreferences
import com.gdavidpb.tuindice.data.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.data.utils.edit
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import java.util.*

open class PreferencesDataStore(
        private val preferences: SharedPreferences
) : SettingsRepository {
    /* Preferences keys */
    private val keyFirstRun = "firstRun"
    private val keyCooldown = "cooldown"
    private val emailSentTo = "getEmailSentTo"

    override suspend fun clearEmailSentTo() {
        preferences.edit {
            remove(emailSentTo)
        }
    }

    override suspend fun getEmailSentTo(): String {
        return preferences.getString(emailSentTo, null) ?: ""
    }

    override suspend fun setEmailSentTo(email: String) {
        preferences.edit {
            putString(emailSentTo, email)
        }
    }

    override suspend fun setCooldown(key: String) {
        val calendar = Calendar.getInstance(DEFAULT_LOCALE)

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        calendar.add(Calendar.DATE, 1)

        preferences.edit {
            putLong("$key$keyCooldown", calendar.timeInMillis)
        }
    }

    override suspend fun isCooldown(key: String): Boolean {
        val now = Calendar.getInstance(DEFAULT_LOCALE)
        val cooldown = Calendar.getInstance(DEFAULT_LOCALE)

        cooldown.timeInMillis = preferences.getLong("$key$keyCooldown", now.timeInMillis)

        return now.before(cooldown)
    }

    override suspend fun isFirstRun(): Boolean {
        return preferences.getBoolean(keyFirstRun, true)
    }

    override suspend fun setFirstRun() {
        return preferences.edit {
            putBoolean(keyFirstRun, true)
        }
    }

    override suspend fun clear() {
        preferences.edit {
            clear()
        }
    }
}