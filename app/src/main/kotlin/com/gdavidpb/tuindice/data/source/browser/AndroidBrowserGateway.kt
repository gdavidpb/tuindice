package com.gdavidpb.tuindice.data.source.browser

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.gdavidpb.tuindice.base.domain.repository.BrowserGateway

class AndroidBrowserGateway(
	private val context: Context
) : BrowserGateway {
	override fun open(url: String) {
		runCatching {
			context.startActivity(
				Intent(Intent.ACTION_VIEW, url.toUri())
			)
		}
	}
}
