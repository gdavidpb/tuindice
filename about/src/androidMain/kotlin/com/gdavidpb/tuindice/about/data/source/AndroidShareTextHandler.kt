package com.gdavidpb.tuindice.about.data.source

import android.content.Context
import android.content.Intent
import com.gdavidpb.tuindice.about.presentation.utils.ShareTextHandler

class AndroidShareTextHandler(
	private val context: Context
) : ShareTextHandler {
	override fun invoke(subject: String, text: String) {
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
}
