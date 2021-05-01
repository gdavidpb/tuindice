package com.gdavidpb.tuindice.data.source.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Credentials
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.utils.ScreenKeys
import com.gdavidpb.tuindice.utils.SettingsKeys
import com.gdavidpb.tuindice.utils.mappers.asUsbEmail
import java.util.*

open class PreferencesDataSource(
        private val preferences: SharedPreferences
) : SettingsRepository {
    override fun startCountdown(reset: Boolean): Long {
        checkIsEncrypted()

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
        checkIsEncrypted()

        preferences.edit {
            remove(SettingsKeys.COUNT_DOWN)
        }
    }

    override fun getEmail(): String {
        checkIsEncrypted()

        return preferences.getString(SettingsKeys.USB_ID, null)?.asUsbEmail() ?: ""
    }

    override fun hasCredentials(): Boolean {
        checkIsEncrypted()

        return preferences.contains(SettingsKeys.USB_ID) && preferences.contains(SettingsKeys.PASSWORD)
    }

    override fun storeCredentials(credentials: Credentials) {
        checkIsEncrypted()

        preferences.edit {
            putString(SettingsKeys.USB_ID, credentials.usbId)
            putString(SettingsKeys.PASSWORD, credentials.password)
        }
    }

    override fun getCredentials(): Credentials {
        checkIsEncrypted()

        return Credentials(
                usbId = preferences.getString(SettingsKeys.USB_ID, null) ?: "",
                password = preferences.getString(SettingsKeys.PASSWORD, null) ?: ""
        )
    }

    override fun getLastScreen(): Int {
        checkIsEncrypted()

        return when (preferences.getInt(SettingsKeys.LAST_SCREEN, R.id.fragment_summary)) {
            ScreenKeys.SUMMARY -> R.id.fragment_summary
            ScreenKeys.RECORD -> R.id.fragment_record
            ScreenKeys.ABOUT -> R.id.fragment_about
            else -> R.id.fragment_summary
        }
    }

    override fun setLastScreen(screen: Int) {
        checkIsEncrypted()

        val screenKey = when (screen) {
            R.id.fragment_summary -> ScreenKeys.SUMMARY
            R.id.fragment_record -> ScreenKeys.RECORD
            R.id.fragment_about -> ScreenKeys.ABOUT
            else -> ScreenKeys.SUMMARY
        }

        preferences.edit {
            putInt(SettingsKeys.LAST_SCREEN, screenKey)
        }
    }

    override fun isReviewSuggested(value: Int): Boolean {
        checkIsEncrypted()

        val counter = preferences.getInt(SettingsKeys.SYNCS_COUNTER, 0) + 1

        preferences.edit {
            putInt(SettingsKeys.SYNCS_COUNTER, counter)
        }

        return counter % value == 0
    }

    override fun storeTopicSubscription(topic: String) {
        checkIsEncrypted()

        val topics = preferences.getStringSet(SettingsKeys.SUBSCRIBED_TOPICS, setOf()) ?: setOf()

        preferences.edit {
            putStringSet(SettingsKeys.SUBSCRIBED_TOPICS, topics + topic)
        }
    }

    override fun isSubscribedToTopic(topic: String): Boolean {
        checkIsEncrypted()

        val topics = preferences.getStringSet(SettingsKeys.SUBSCRIBED_TOPICS, setOf()) ?: setOf()

        return topics.contains(topic)
    }

    override fun clear() {
        checkIsEncrypted()

        preferences.edit {
            clear()
        }
    }

    private fun checkIsEncrypted() {
        check(preferences is EncryptedSharedPreferences) { "preferences encryption failed" }
    }
}