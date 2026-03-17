package com.gdavidpb.tuindice.data.source.browser

import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.platform.IosExternalActionsCapability

class IosBrowserDataSource(
	private val externalActionsCapability: IosExternalActionsCapability
) : BrowserRepository {
	override fun open(url: String) {
		externalActionsCapability.openUrl(url)
	}
}
