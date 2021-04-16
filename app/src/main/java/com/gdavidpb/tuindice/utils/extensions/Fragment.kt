package com.gdavidpb.tuindice.utils.extensions

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.gdavidpb.tuindice.R

fun Fragment.selector(
        @StringRes textResource: Int,
        items: Array<String>,
        onClick: (String) -> Unit
) = requireActivity().selector(textResource, items, onClick)

fun Fragment.email(email: String, subject: String = "", text: String = "") = requireContext().email(email, subject, text)

fun Fragment.share(text: String, subject: String = "") = requireContext().share(text, subject)

fun Fragment.requireAppCompatActivity() = requireActivity() as AppCompatActivity

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