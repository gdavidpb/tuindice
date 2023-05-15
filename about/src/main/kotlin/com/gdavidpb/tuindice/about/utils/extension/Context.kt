package com.gdavidpb.tuindice.about.utils.extension

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.gdavidpb.tuindice.about.R
import com.gdavidpb.tuindice.base.utils.extension.browse

fun Context.playStore() {
	val uri = Uri.parse(getString(R.string.about_google_play_intent, packageName))
	val intent = Intent(Intent.ACTION_VIEW, uri).apply {
		addFlags(
			Intent.FLAG_ACTIVITY_NO_HISTORY or
					Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
					Intent.FLAG_ACTIVITY_NEW_DOCUMENT
		)
	}

	runCatching {
		startActivity(intent)
	}.onFailure {
		browse(url = getString(R.string.about_google_play, packageName))
	}
}

fun Context.email(email: String, subject: String = "", text: String = "") {
	runCatching {
		val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
			putExtra(Intent.EXTRA_EMAIL, arrayOf(email))

			if (subject.isNotEmpty()) putExtra(Intent.EXTRA_SUBJECT, subject)
			if (text.isNotEmpty()) putExtra(Intent.EXTRA_TEXT, text)
		}

		startActivity(intent)
	}
}

fun Context.share(subject: String = "", text: String) {
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