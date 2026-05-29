package com.gdavidpb.tuindice.data.source.performance

import com.gdavidpb.tuindice.base.data.source.usage.UsageDataCollectionControllerDataSource
import com.gdavidpb.tuindice.base.domain.controller.UsageDataCollectionController
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.google.firebase.perf.FirebasePerformance

class FirebasePerformanceCollectionController(
	firebasePerformance: FirebasePerformance,
	usageDataConsentRepository: UsageDataConsentRepository
) : UsageDataCollectionController {
	private val delegate = UsageDataCollectionControllerDataSource(
		usageDataConsentRepository = usageDataConsentRepository,
		setCollectionEnabled = firebasePerformance::setPerformanceCollectionEnabled
	)

	override fun start() {
		delegate.start()
	}
}
