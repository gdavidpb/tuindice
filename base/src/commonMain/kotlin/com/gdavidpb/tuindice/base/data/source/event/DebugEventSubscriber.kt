package com.gdavidpb.tuindice.base.data.source.event

import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import com.gdavidpb.tuindice.base.logging.appLogger

class DebugEventSubscriber(
	private val sourceName: String,
	private val usageDataConsentRepository: UsageDataConsentRepository
) : EventSubscriber {
	private val logger = appLogger(tag = "AppEvents")

	override val id: String = "$sourceName-debug"

	override val isEnabled: Boolean
		get() = usageDataConsentRepository.isUsageDataCollectionEnabled()

	override fun onEvent(event: AppEvent) {
		logger.i { "[$sourceName] ${event.name} ${event.parameters}" }
	}
}
