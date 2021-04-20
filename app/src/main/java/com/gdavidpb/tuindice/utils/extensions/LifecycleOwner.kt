package com.gdavidpb.tuindice.utils.extensions

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.gdavidpb.tuindice.BuildConfig
import java.io.File

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
): AlertDialog {
    return when (this) {
        is Fragment -> alert {
            titleResource = textResource

            setItems(items) { _, which -> onClick(items[which]) }
        }
        is FragmentActivity -> alert {
            titleResource = textResource

            setItems(items) { _, which -> onClick(items[which]) }
        }
        else -> throw NoWhenBranchMatchedException()
    }
}

private fun LifecycleOwner.startActivity(intent: Intent) = when (this) {
    is FragmentActivity -> startActivity(intent)
    is Fragment -> startActivity(intent)
    else -> throw NoWhenBranchMatchedException()
}

private fun LifecycleOwner.context() = when (this) {
    is FragmentActivity -> this
    is Fragment -> requireContext()
    else -> throw NoWhenBranchMatchedException()
}