package com.gdavidpb.tuindice.base.data.source.usage

import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryUsageDataConsentRepository(
	initialValue: Boolean = false
) : UsageDataConsentRepository {
	private val usageDataEnabled = MutableStateFlow(initialValue)

	override val usageDataCollectionEnabled: StateFlow<Boolean> =
		usageDataEnabled.asStateFlow()

	override fun isUsageDataCollectionEnabled(): Boolean =
		usageDataEnabled.value

	override fun setUsageDataCollectionEnabled(enabled: Boolean) {
		usageDataEnabled.value = enabled
	}
}
