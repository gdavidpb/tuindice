package com.gdavidpb.tuindice.data.source.analytics

import com.gdavidpb.tuindice.base.data.source.event.isAnalyticsRelevant
import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import com.gdavidpb.tuindice.platform.IosObservabilityCapability

class IosAnalyticsEventSubscriber(
	private val observabilityCapability: IosObservabilityCapability
) : EventSubscriber {
	override val id: String = "firebase_analytics_ios"

	override val isEnabled: Boolean = true

	override fun onEvent(event: AppEvent) {
		if (!event.isAnalyticsRelevant()) return

		observabilityCapability.logEvent(
			name = event.name,
			parameters = event.parameters
		)
	}
}
