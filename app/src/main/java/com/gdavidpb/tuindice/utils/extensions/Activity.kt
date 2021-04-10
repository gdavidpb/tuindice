package com.gdavidpb.tuindice.utils.extensions

import android.app.ActivityManager
import android.content.Intent
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.RequestCodes
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import org.koin.android.ext.android.inject

inline val FragmentActivity.contentView: View?
    get() = findViewById<ViewGroup>(android.R.id.content)?.getChildAt(0)

fun AppCompatActivity.clearApplicationUserData() {
    val activityManager by inject<ActivityManager>()

    activityManager.clearApplicationUserData()
}

fun AppCompatActivity.hideSoftKeyboard() {
    val inputMethodManager by inject<InputMethodManager>()

    inputMethodManager.hideSoftKeyboard(this)
}

fun FragmentActivity.selector(
        @StringRes textResource: Int,
        items: Array<String>,
        onClick: (String) -> Unit
): AlertDialog = alert {
    titleResource = textResource

    setItems(items) { _, which -> onClick(items[which]) }
}

fun FragmentActivity.openDataTime() {
    val intent = Intent(Settings.ACTION_DATE_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    startActivityForResult(intent, 0)
}

fun FragmentActivity.isGoogleServicesAvailable(): Boolean {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val status = googleApiAvailability.isGooglePlayServicesAvailable(this)

    return (status == ConnectionResult.SUCCESS).also { available ->
        if (!available)
            if (googleApiAvailability.isUserResolvableError(status))
                googleApiAvailability.getErrorDialog(
                        this,
                        status,
                        RequestCodes.PLAY_SERVICES_RESOLUTION
                ).apply {
                    setOnCancelListener { finish() }
                    setOnDismissListener { finish() }
                }.show()
            else
                alert {
                    titleResource = R.string.alert_title_no_gms_failure
                    messageResource = R.string.alert_message_no_gms_failure

                    isCancelable = false

                    positiveButton(R.string.exit) {
                        finish()
                    }
                }
    }
}