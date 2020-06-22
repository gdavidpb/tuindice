package com.gdavidpb.tuindice.migrations

import android.app.ActivityManager
import android.content.Context
import androidx.core.content.edit
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.KEY_LAST_SCREEN
import com.gdavidpb.tuindice.utils.KEY_PASSWORD
import com.gdavidpb.tuindice.utils.KEY_USB_ID
import com.gdavidpb.tuindice.utils.extensions.encryptedSharedPreferences
import com.gdavidpb.tuindice.utils.extensions.sharedPreferences
import com.gdavidpb.tuindice.utils.extensions.shred
import java.io.File

class SharedPreferencesMigration(private val context: Context) : Migration() {
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

        val usbId = sourceSharedPreferences.getString(KEY_USB_ID, null)
        val password = sourceSharedPreferences.getString(KEY_PASSWORD, null)
        val lastScreen = sourceSharedPreferences.getInt(KEY_LAST_SCREEN, R.id.nav_summary)

        targetSharedPreferences.edit {
            if (usbId != null) putString(KEY_USB_ID, usbId)
            if (password != null) putString(KEY_PASSWORD, password)

            putInt(KEY_LAST_SCREEN, lastScreen)
        }

        sharedPrefsFile.shred()
    }

    override fun onFailure(throwable: Throwable) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        activityManager.clearApplicationUserData()
    }
}