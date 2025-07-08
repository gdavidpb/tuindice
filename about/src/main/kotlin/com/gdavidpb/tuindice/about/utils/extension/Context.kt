package com.gdavidpb.tuindice.about.utils.extension

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.gdavidpb.tuindice.about.R
import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.utils.extension.browse
import com.gdavidpb.tuindice.base.utils.extension.versionCode
import com.gdavidpb.tuindice.base.utils.extension.versionName

fun Context.playStore() {
	val uri = getString(R.string.about_google_play_intent, packageName).toUri()
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
		val intent = Intent(Intent.ACTION_SENDTO, "mailto:".toUri()).apply {
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

fun Context.versionDescription(): String {
	val environmentRes = if (BuildConfig.DEBUG) R.string.debug else R.string.release
	val environmentName = getString(environmentRes)

	return getString(
		R.string.app_version,
		environmentName,
		versionName(),
		versionCode()
	)
}