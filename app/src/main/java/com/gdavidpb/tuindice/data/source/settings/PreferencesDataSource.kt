package com.gdavidpb.tuindice.data.source.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.service.DstCredentials
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.utils.SettingsKeys
import com.gdavidpb.tuindice.utils.mappers.toRefYear
import com.gdavidpb.tuindice.utils.mappers.toUsbEmail
import java.util.*

open class PreferencesDataSource(
        private val preferences: SharedPreferences
) : SettingsRepository {
    override fun startCountdown(reset: Boolean): Long {
        val countdown = preferences.getLong(SettingsKeys.COUNT_DOWN, 0L)

        return if (countdown == 0L || reset) {
            val time = Date().time

            preferences.edit {
                putLong(SettingsKeys.COUNT_DOWN, time)
            }

            time
        } else {
            countdown
        }
    }

    override fun resetCountdown() {
        preferences.edit {
            remove(SettingsKeys.COUNT_DOWN)
        }
    }

    override fun getEmail(): String {
        return preferences.getString(SettingsKeys.USB_ID, null)?.toUsbEmail() ?: ""
    }

    override fun hasCredentials(): Boolean {
        return preferences.contains(SettingsKeys.USB_ID) && preferences.contains(SettingsKeys.PASSWORD)
    }

    override fun storeCredentials(credentials: DstCredentials) {
        preferences.edit {
            putString(SettingsKeys.USB_ID, credentials.usbId)
            putString(SettingsKeys.PASSWORD, credentials.password)
        }
    }

    override fun getCredentials(): DstCredentials {
        return DstCredentials(
                usbId = preferences.getString(SettingsKeys.USB_ID, null) ?: "",
                password = preferences.getString(SettingsKeys.PASSWORD, null) ?: ""
        )
    }

    override fun getCredentialYear(): Int {
        val usbId = preferences.getString(SettingsKeys.USB_ID, null) ?: ""

        return if (usbId.isNotEmpty()) usbId.toRefYear() else -1
    }

    override fun getLastScreen(): Int {
        return preferences.getInt(SettingsKeys.LAST_SCREEN, R.id.fragment_summary)
    }

    override fun setLastScreen(screen: Int) {
        preferences.edit {
            putInt(SettingsKeys.LAST_SCREEN, screen)
        }
    }

    override fun isReviewSuggested(value: Int): Boolean {
        val counter = preferences.getInt(SettingsKeys.SYNCS_COUNTER, 0) + 1

        preferences.edit {
            putInt(SettingsKeys.SYNCS_COUNTER, counter)
        }

        return counter == value
    }

    override fun clear() {
        preferences.edit {
            clear()
        }
    }
}