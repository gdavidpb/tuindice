package com.gdavidpb.tuindice.base.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface UsageDataConsentRepository {
	val usageDataCollectionEnabled: StateFlow<Boolean>

	fun isUsageDataCollectionEnabled(): Boolean

	fun setUsageDataCollectionEnabled(enabled: Boolean)
}
