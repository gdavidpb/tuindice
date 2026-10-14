package com.gdavidpb.tuindice.about.data.source

import com.gdavidpb.tuindice.about.presentation.utils.ShareTextHandler
import com.gdavidpb.tuindice.base.ui.presentActivitySheet

class IosShareTextHandler : ShareTextHandler {
	override fun invoke(subject: String, text: String) {
		presentActivitySheet(activityItems = listOf(text))
	}
}
