package com.gdavidpb.tuindice.data.source.settings

import android.content.SharedPreferences
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.service.DstCredentials
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.edit
import com.gdavidpb.tuindice.utils.mappers.toRefYear
import java.util.*

open class PreferencesDataStore(
        private val preferences: SharedPreferences
) : SettingsRepository {

    override fun awaitingEmail(): String {
        return preferences.getString(KEY_AWAITING_EMAIL, null) ?: ""
    }

    override fun awaitingPassword(): String {
        return preferences.getString(KEY_AWAITING_PASSWORD, null) ?: ""
    }

    override fun setIsAwaitingForReset(email: String, password: String) {
        preferences.edit {
            putString(KEY_AWAITING_EMAIL, email)
            putString(KEY_AWAITING_PASSWORD, password)
        }
    }

    override fun isAwaitingForReset(): Boolean {
        val email = preferences.getString(KEY_AWAITING_EMAIL, null) ?: ""
        val password = preferences.getString(KEY_AWAITING_PASSWORD, null) ?: ""

        return email.isNotEmpty() && password.isNotEmpty()
    }

    override fun clearIsAwaitingForReset() {
        preferences.edit {
            remove(KEY_AWAITING_EMAIL)
            remove(KEY_AWAITING_PASSWORD)
        }
    }

    override fun startCountdown(reset: Boolean): Long {
        val countdown = preferences.getLong(KEY_COUNT_DOWN, 0L)

        return if (countdown == 0L || reset) {
            val time = Date().time

            preferences.edit {
                putLong(KEY_COUNT_DOWN, time)
            }

            time
        } else {
            countdown
        }
    }

    override fun storeCredentials(credentials: DstCredentials) {
        preferences.edit {
            putString(KEY_USB_ID, credentials.usbId)
            putString(KEY_PASSWORD, credentials.password)
        }
    }

    override fun getCredentials(): DstCredentials {
        return DstCredentials(
                usbId = preferences.getString(KEY_USB_ID, null) ?: "",
                password = preferences.getString(KEY_PASSWORD, null) ?: ""
        )
    }

    override fun getCredentialYear(): Int {
        val usbId = preferences.getString(KEY_USB_ID, null) ?: ""

        return if (usbId.isNotEmpty()) usbId.toRefYear() else -1
    }

    override fun getLastScreen(): Int {
        return preferences.getInt(KEY_LAST_SCREEN, R.id.nav_summary)
    }

    override fun setLastScreen(screen: Int) {
        preferences.edit {
            putInt(KEY_LAST_SCREEN, screen)
        }
    }

    override fun clear() {
        preferences.edit {
            clear()
        }
    }
}