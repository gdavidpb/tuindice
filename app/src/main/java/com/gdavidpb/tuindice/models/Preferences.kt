package com.gdavidpb.tuindice.models

import android.content.Context
import android.preference.PreferenceManager
import com.gdavidpb.tuindice.Constants
import com.gdavidpb.tuindice.models.Preferences.Companion.getInstance
import java.util.*

val Context.preferences: Preferences
    get() = getInstance(applicationContext)

class Preferences(preferences: android.content.SharedPreferences)
    : com.gdavidpb.tuindice.abstracts.SharedPreferences(preferences) {

    companion object {
        private var instance: Preferences? = null

        @Synchronized
        fun getInstance(context: Context): Preferences {
            if (instance == null)
                instance = Preferences(PreferenceManager
                        .getDefaultSharedPreferences(context))

            return instance!!
        }
    }


    fun firstRun() {
        edit().putBoolean(Constants.KEY_FIRST_RUN, false)
    }

    fun resetConnectionRetry() {
        edit().putInt(Constants.KEY_CONNECTION_RETRY, Constants.DEFAULT_CONNECTION_RETRY)
    }

    fun setCooldown() {
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        calendar.add(Calendar.DATE, 1)

        edit().putLong(Constants.KEY_REFRESH_COOLDOWN, calendar.timeInMillis)
    }

    fun isCooldown(): Boolean {
        val now = Calendar.getInstance()
        val cooldown = Calendar.getInstance()

        cooldown.timeInMillis = getLong(Constants.KEY_REFRESH_COOLDOWN, now.timeInMillis)

        return now.before(cooldown)
    }

    fun getFirstRun(): Boolean = getBoolean(Constants.KEY_FIRST_RUN, true)

    fun connectionRetryEnough(): Boolean {
        val retry = (getInt(Constants.KEY_CONNECTION_RETRY, Constants.DEFAULT_CONNECTION_RETRY) - 1)

        edit().putInt(Constants.KEY_CONNECTION_RETRY, retry)

        return retry <= 0
    }
}