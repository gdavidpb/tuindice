package com.gdavidpb.tuindice.base.data.source.usage

import com.gdavidpb.tuindice.base.domain.startup.AppStartupTask

object NoOpUsageDataCollectionDataSource : AppStartupTask {
	override fun start() = Unit
}
