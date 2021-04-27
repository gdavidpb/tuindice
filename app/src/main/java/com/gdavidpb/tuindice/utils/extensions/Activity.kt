package com.gdavidpb.tuindice.utils.extensions

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.ui.dialogs.ConfirmationBottomSheetDialog
import com.gdavidpb.tuindice.utils.RequestCodes
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

inline val FragmentActivity.contentView: View?
    get() = findViewById<ViewGroup>(android.R.id.content)?.getChildAt(0)

fun FragmentActivity.isGoogleServicesAvailable(): Boolean {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val status = googleApiAvailability.isGooglePlayServicesAvailable(this)

    return (status == ConnectionResult.SUCCESS).also { available ->
        if (!available)
            if (googleApiAvailability.isUserResolvableError(status))
                googleApiAvailability.getErrorDialog(
                        this,
                        status,
                        RequestCodes.PLAY_SERVICES_RESOLUTION_REQUEST
                ).apply {
                    setOnCancelListener { finish() }
                    setOnDismissListener { finish() }
                }.show()
            else
                bottomSheetDialog<ConfirmationBottomSheetDialog> {
                    titleResource = R.string.dialog_title_no_gms_failure
                    messageResource = R.string.dialog_message_no_gms_failure

                    positiveButton(R.string.exit) { finish() }
                }.apply {
                    isCancelable = false
                }
    }
}