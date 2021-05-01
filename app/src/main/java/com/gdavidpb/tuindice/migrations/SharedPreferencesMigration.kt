package com.gdavidpb.tuindice.migrations

import android.app.ActivityManager
import android.content.Context
import androidx.core.content.edit
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.utils.ScreenKeys
import com.gdavidpb.tuindice.utils.SettingsKeys
import com.gdavidpb.tuindice.utils.extensions.encryptedSharedPreferences
import com.gdavidpb.tuindice.utils.extensions.sharedPreferences
import com.gdavidpb.tuindice.utils.extensions.shred
import java.io.File

class SharedPreferencesMigration(private val context: Context) : Migration() {
    object Settings {
        const val SHRED_PASSES = 35
    }

    private val sharedPrefsDir by lazy {
        File(context.applicationInfo.dataDir, "shared_prefs")
    }

    private val sharedPrefsFile by lazy {
        File(sharedPrefsDir, "${BuildConfig.APPLICATION_ID}_preferences.xml")
    }

    override fun isRequired(): Boolean {
        return sharedPrefsFile.exists()
    }

    override fun onMigrate() {
        val sourceSharedPreferences = context.sharedPreferences()
        val targetSharedPreferences = context.encryptedSharedPreferences()

        val usbId = sourceSharedPreferences.getString(SettingsKeys.USB_ID, null)
        val password = sourceSharedPreferences.getString(SettingsKeys.PASSWORD, null)
        val lastScreen = sourceSharedPreferences.getInt(SettingsKeys.LAST_SCREEN, ScreenKeys.SUMMARY)

        targetSharedPreferences.edit {
            if (usbId != null) putString(SettingsKeys.USB_ID, usbId)
            if (password != null) putString(SettingsKeys.PASSWORD, password)

            putInt(SettingsKeys.LAST_SCREEN, lastScreen)
        }

        sharedPrefsFile.shred(passes = Settings.SHRED_PASSES)
    }

    override fun onFailure(throwable: Throwable) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        activityManager.clearApplicationUserData()
    }
}