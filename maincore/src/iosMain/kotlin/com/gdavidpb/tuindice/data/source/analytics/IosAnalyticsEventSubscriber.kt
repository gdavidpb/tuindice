package com.gdavidpb.tuindice.data.source.analytics

import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.AnalyticsConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import com.gdavidpb.tuindice.platform.IosObservabilityCapability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class IosAnalyticsEventSubscriber(
	private val observabilityCapability: IosObservabilityCapability,
	private val analyticsConsentRepository: AnalyticsConsentRepository
) : EventSubscriber {
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

	init {
		observabilityCapability.setAnalyticsCollectionEnabled(
			analyticsConsentRepository.isAnalyticsCollectionEnabled()
		)
		scope.launch {
			analyticsConsentRepository.analyticsCollectionEnabled.collectLatest { enabled ->
				observabilityCapability.setAnalyticsCollectionEnabled(enabled)
			}
		}
	}

	override val id: String = "firebase_analytics_ios"

	override val isEnabled: Boolean
		get() = analyticsConsentRepository.isAnalyticsCollectionEnabled()

	override fun onEvent(event: AppEvent) {
		if (!isEnabled) return

		observabilityCapability.logEvent(
			name = event.name,
			parameters = event.parameters
		)
	}
}
