package com.gdavidpb.tuindice.base.data.source.usage

import com.gdavidpb.tuindice.base.domain.controller.UsageDataCollectionController

object NoOpUsageDataCollectionController : UsageDataCollectionController {
	override fun start() = Unit
}
