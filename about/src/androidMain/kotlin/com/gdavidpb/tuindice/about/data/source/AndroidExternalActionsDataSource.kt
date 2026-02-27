package com.gdavidpb.tuindice.about.data.source

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.gdavidpb.tuindice.about.domain.repository.ExternalActionsRepository

class AndroidExternalActionsDataSource(
	private val context: Context
) : ExternalActionsRepository {
	override fun sendEmail(email: String, subject: String, text: String) {
		val intent = Intent(Intent.ACTION_SENDTO, "mailto:".toUri()).apply {
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			putExtra(Intent.EXTRA_EMAIL, arrayOf(email))

			if (subject.isNotEmpty()) putExtra(Intent.EXTRA_SUBJECT, subject)
			if (text.isNotEmpty()) putExtra(Intent.EXTRA_TEXT, text)
		}

		context.startActivity(intent)
	}

	override fun shareText(subject: String, text: String) {
		val sendIntent = Intent(Intent.ACTION_SEND).apply {
			type = "text/plain"
			putExtra(Intent.EXTRA_SUBJECT, subject)
			putExtra(Intent.EXTRA_TEXT, text)
		}

		val chooserIntent = Intent.createChooser(sendIntent, null).apply {
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		}

		context.startActivity(chooserIntent)
	}

	override fun openStore() {
		val packageName = context.packageName
		val intent = Intent(
			Intent.ACTION_VIEW,
			"market://details?id=$packageName".toUri()
		).apply {
			addFlags(
				Intent.FLAG_ACTIVITY_NEW_TASK or
						Intent.FLAG_ACTIVITY_NO_HISTORY or
						Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
						Intent.FLAG_ACTIVITY_NEW_DOCUMENT
			)
		}

		runCatching {
			context.startActivity(intent)
		}.onFailure {
			val url = "https://play.google.com/store/apps/details?id=$packageName"
			val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
				addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			}

			context.startActivity(intent)
		}
	}
}
