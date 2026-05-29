package com.gdavidpb.tuindice.base.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface AnalyticsConsentRepository {
	val analyticsCollectionEnabled: StateFlow<Boolean>

	fun isAnalyticsCollectionEnabled(): Boolean

	fun setAnalyticsCollectionEnabled(enabled: Boolean)
}
