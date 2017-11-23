package com.gdavidpb.tuindice.models

import android.content.Context
import android.preference.PreferenceManager
import com.gdavidpb.tuindice.Constants

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

    fun getFirstRun(): Boolean = getBoolean(Constants.KEY_FIRST_RUN, true)

    fun resetConnectionRetry() {
        edit().putInt(Constants.KEY_CONNECTION_RETRY, Constants.DEFAULT_CONNECTION_RETRY)
    }

    fun connectionRetryEnough(): Boolean {
        val retry = (getInt(Constants.KEY_CONNECTION_RETRY, Constants.DEFAULT_CONNECTION_RETRY) - 1)

        edit().putInt(Constants.KEY_CONNECTION_RETRY, retry)

        return retry <= 0
    }

    fun firstRun() {
        edit().putBoolean(Constants.KEY_FIRST_RUN, false)
    }
}