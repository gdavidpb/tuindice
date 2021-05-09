package com.gdavidpb.tuindice.utils.extensions

import android.content.Intent
import android.net.Uri
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File
import kotlin.reflect.full.createInstance

fun LifecycleOwner.hideSoftKeyboard(inputMethodManager: InputMethodManager) {
    val view = when (this) {
        is Fragment -> requireView().rootView
        is FragmentActivity -> currentFocus
        else -> throw NoWhenBranchMatchedException()
    }

    view?.hideSoftKeyboard(inputMethodManager)
}

fun LifecycleOwner.openPdf(file: File) {
    val uri = FileProvider.getUriForFile(context(), BuildConfig.APPLICATION_ID, file)

    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
    }

    startActivity(intent)
}

fun LifecycleOwner.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context(), resId, duration).show()
}

fun LifecycleOwner.browse(url: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        startActivity(intent)
    }
}

fun LifecycleOwner.share(text: String, subject: String = "") {
    runCatching {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }.let {
            Intent.createChooser(it, null)
        }

        startActivity(intent)
    }
}

fun LifecycleOwner.email(email: String, subject: String = "", text: String = "") {
    runCatching {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))

            if (subject.isNotEmpty()) putExtra(Intent.EXTRA_SUBJECT, subject)
            if (text.isNotEmpty()) putExtra(Intent.EXTRA_TEXT, text)
        }

        startActivity(intent)
    }
}

fun LifecycleOwner.selector(
        @StringRes textResource: Int,
        items: Array<String>,
        onClick: (String) -> Unit
): AlertDialog = AlertDialog.Builder(context()).apply {
    setTitle(textResource)

    setItems(items) { _, which -> onClick(items[which]) }
}.show()

fun LifecycleOwner.connectionSnackBar(isNetworkAvailable: Boolean, retry: (() -> Unit)? = null) {
    val message = if (isNetworkAvailable)
        R.string.snack_service_unreachable
    else
        R.string.snack_network_unavailable

    errorSnackBar(message, retry)
}

fun LifecycleOwner.errorSnackBar(@StringRes message: Int = R.string.snack_default_error, retry: (() -> Unit)? = null) {
    snackBar {
        messageResource = message

        if (retry != null) action(R.string.retry) { retry() }
    }
}

inline fun <reified T : BottomSheetDialogFragment> LifecycleOwner.bottomSheetDialog(
        builder: T.() -> Unit
): BottomSheetDialogFragment {
    val fragmentManager = when (this) {
        is Fragment -> childFragmentManager
        is FragmentActivity -> supportFragmentManager
        else -> throw NoWhenBranchMatchedException()
    }

    val dialog = T::class.createInstance()
    val tag = T::class.simpleName

    return dialog.apply {
        builder()

        show(fragmentManager, tag)
    }
}

fun LifecycleOwner.startActivity(intent: Intent) = when (this) {
    is Fragment -> startActivity(intent)
    is FragmentActivity -> startActivity(intent)
    else -> throw NoWhenBranchMatchedException()
}

fun LifecycleOwner.context() = when (this) {
    is Fragment -> requireContext()
    is FragmentActivity -> this
    else -> throw NoWhenBranchMatchedException()
}