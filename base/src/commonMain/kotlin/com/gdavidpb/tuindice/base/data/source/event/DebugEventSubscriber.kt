package com.gdavidpb.tuindice.base.data.source.event

import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.AnalyticsConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import com.gdavidpb.tuindice.base.logging.appLogger

class DebugEventSubscriber(
	private val sourceName: String,
	private val analyticsConsentRepository: AnalyticsConsentRepository
) : EventSubscriber {
	private val logger = appLogger(tag = "AppEvents")

	override val id: String = "$sourceName-debug"

	override val isEnabled: Boolean
		get() = analyticsConsentRepository.isAnalyticsCollectionEnabled()

	override fun onEvent(event: AppEvent) {
		logger.i { "[$sourceName] ${event.name} ${event.parameters}" }
	}
}
