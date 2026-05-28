package com.gdavidpb.tuindice.data.source.browser

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository

class AndroidBrowserDataSource(
	private val context: Context
) : BrowserRepository {
	override fun open(url: String) {
		context.startActivity(
			Intent(Intent.ACTION_VIEW, url.toUri()).apply {
				addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			}
		)
	}
}
