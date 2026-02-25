package com.gdavidpb.tuindice.data.source.browser

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository

class AndroidBrowserGateway(
	private val context: Context
) : BrowserRepository {
	override fun open(url: String) {
		runCatching {
			context.startActivity(
				Intent(Intent.ACTION_VIEW, url.toUri())
			)
		}
	}
}
