package com.gdavidpb.tuindice.about.utils.extensions

import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import com.gdavidpb.tuindice.base.utils.extensions.context
import com.gdavidpb.tuindice.base.utils.extensions.startActivity

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