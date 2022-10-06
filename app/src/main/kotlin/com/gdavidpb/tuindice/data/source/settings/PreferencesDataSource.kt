package com.gdavidpb.tuindice.data.source.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.utils.ScreenKeys
import com.gdavidpb.tuindice.base.utils.SettingsKeys

open class PreferencesDataSource(
        private val preferences: SharedPreferences
) : SettingsRepository {
    override fun getLastScreen(): Int {
        return when (preferences.getInt(SettingsKeys.LAST_SCREEN, R.id.fragment_summary)) {
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

        preferences.edit {
            putInt(SettingsKeys.LAST_SCREEN, screenKey)
        }
    }

    override fun isReviewSuggested(value: Int): Boolean {
        val counter = preferences.getInt(SettingsKeys.SYNCS_COUNTER, 0) + 1

        preferences.edit {
            putInt(SettingsKeys.SYNCS_COUNTER, counter)
        }

        return counter % value == 0
    }

    override fun saveSubscriptionTopic(topic: String) {
        val topics = preferences.getStringSet(SettingsKeys.SUBSCRIBED_TOPICS, setOf()) ?: setOf()

        preferences.edit {
            putStringSet(SettingsKeys.SUBSCRIBED_TOPICS, topics + topic)
        }
    }

    override fun isSubscribedToTopic(topic: String): Boolean {
        val topics = preferences.getStringSet(SettingsKeys.SUBSCRIBED_TOPICS, setOf()) ?: setOf()

        return topics.contains(topic)
    }

    override fun clear() {
        preferences.edit {
            clear()
        }
    }
}