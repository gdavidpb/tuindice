package com.gdavidpb.tuindice.utils.extensions

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.gdavidpb.tuindice.R

fun Fragment.connectionSnackBar(isNetworkAvailable: Boolean, retry: (() -> Unit)? = null) {
    val message = if (isNetworkAvailable)
        R.string.snack_service_unreachable
    else
        R.string.snack_network_unavailable

    errorSnackBar(message, retry)
}

fun Fragment.errorSnackBar(@StringRes message: Int = R.string.snack_default_error, retry: (() -> Unit)? = null) {
    snackBar {
        messageResource = message

        if (retry != null) action(R.string.retry) { retry() }
    }
}