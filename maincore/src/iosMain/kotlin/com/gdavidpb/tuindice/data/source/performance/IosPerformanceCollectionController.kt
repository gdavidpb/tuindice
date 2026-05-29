package com.gdavidpb.tuindice.data.source.performance

import com.gdavidpb.tuindice.base.data.source.usage.UsageDataCollectionControllerDataSource
import com.gdavidpb.tuindice.base.domain.controller.UsageDataCollectionController
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.gdavidpb.tuindice.platform.IosObservabilityCapability

class IosPerformanceCollectionController(
	observabilityCapability: IosObservabilityCapability,
	usageDataConsentRepository: UsageDataConsentRepository
) : UsageDataCollectionController {
	private val delegate = UsageDataCollectionControllerDataSource(
		usageDataConsentRepository = usageDataConsentRepository,
		setCollectionEnabled = observabilityCapability::setPerformanceCollectionEnabled
	)

	override fun start() {
		delegate.start()
	}
}
