package com.gdavidpb.tuindice.data.source.analytics

import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import com.gdavidpb.tuindice.platform.IosObservabilityCapability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class IosAnalyticsEventSubscriber(
	private val observabilityCapability: IosObservabilityCapability,
	private val usageDataConsentRepository: UsageDataConsentRepository
) : EventSubscriber {
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

	init {
		observabilityCapability.setUsageDataCollectionEnabled(
			usageDataConsentRepository.isUsageDataCollectionEnabled()
		)
		scope.launch {
			usageDataConsentRepository.usageDataCollectionEnabled.collectLatest { enabled ->
				observabilityCapability.setUsageDataCollectionEnabled(enabled)
			}
		}
	}

	override val id: String = "firebase_analytics_ios"

	override val isEnabled: Boolean
		get() = usageDataConsentRepository.isUsageDataCollectionEnabled()

	override fun onEvent(event: AppEvent) {
		if (!isEnabled) return

		observabilityCapability.logEvent(
			name = event.name,
			parameters = event.parameters
		)
	}
}
