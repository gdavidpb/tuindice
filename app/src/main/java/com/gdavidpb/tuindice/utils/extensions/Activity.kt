package com.gdavidpb.tuindice.utils.extensions

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.PLAY_SERVICES_RESOLUTION_REQUEST
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import org.koin.android.ext.android.inject

inline val FragmentActivity.contentView: View?
    get() = findViewById<ViewGroup>(android.R.id.content)?.getChildAt(0)

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

fun FragmentActivity.openSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    startActivityForResult(intent, 0)
}

fun FragmentActivity.showSnackBarException(throwable: Throwable? = null, retryAction: (() -> Unit)? = null) {
    val connectivityManager by inject<ConnectivityManager>()
    val isNetworkAvailable = connectivityManager.isNetworkAvailable()

    snackBar {
        messageResource = when {
            throwable == null -> R.string.snack_bar_error_occurred
            throwable.isNoNetworkAvailableIssue(isNetworkAvailable) -> R.string.snack_network_unavailable
            throwable.isConnectionIssue() -> R.string.snack_service_unreachable
            throwable.isPermissionDenied() -> R.string.snack_bar_permission_denied
            throwable.isInvalidCredentials() -> R.string.snack_invalid_credentials
            else -> R.string.snack_bar_error_occurred
        }

        if (retryAction != null)
            action(R.string.retry) {
                retryAction()
            }
    }
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
                        PLAY_SERVICES_RESOLUTION_REQUEST
                ).apply {
                    setOnCancelListener { finish() }
                    setOnDismissListener { finish() }
                }.show()
            else
                alert {
                    titleResource = R.string.alert_title_no_services_failure
                    messageResource = R.string.alert_message_no_services_failure

                    isCancelable = false

                    positiveButton(R.string.exit) {
                        finish()
                    }
                }
    }
}