package com.gdavidpb.tuindice.base.data.source.event

import com.gdavidpb.tuindice.base.domain.repository.AnalyticsConsentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryAnalyticsConsentRepository(
	initialValue: Boolean = false
) : AnalyticsConsentRepository {
	private val analyticsEnabled = MutableStateFlow(initialValue)

	override val analyticsCollectionEnabled: StateFlow<Boolean> =
		analyticsEnabled.asStateFlow()

	override fun isAnalyticsCollectionEnabled(): Boolean =
		analyticsEnabled.value

	override fun setAnalyticsCollectionEnabled(enabled: Boolean) {
		analyticsEnabled.value = enabled
	}
}
