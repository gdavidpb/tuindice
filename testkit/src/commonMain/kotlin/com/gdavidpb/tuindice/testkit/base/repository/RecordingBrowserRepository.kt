package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class RecordingBrowserRepository : BrowserRepository {
	var lastOpenedUrl: String? = null
	val openedUrls = MutableStateFlow<List<String>>(emptyList())

	override fun open(url: String) {
		lastOpenedUrl = url
		openedUrls.update { urls -> urls + url }
	}
}
