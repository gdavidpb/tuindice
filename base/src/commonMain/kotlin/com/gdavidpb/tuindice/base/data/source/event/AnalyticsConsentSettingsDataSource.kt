package com.gdavidpb.tuindice.base.data.source.event

import com.gdavidpb.tuindice.base.domain.repository.AnalyticsConsentRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AnalyticsConsentSettingsDataSource(
	private val settings: Settings
) : AnalyticsConsentRepository {
	private val analyticsEnabled = MutableStateFlow(
		settings.getBooleanOrNull(ANALYTICS_COLLECTION_ENABLED_KEY) ?: false
	)

	override val analyticsCollectionEnabled: StateFlow<Boolean> =
		analyticsEnabled.asStateFlow()

	override fun isAnalyticsCollectionEnabled(): Boolean =
		analyticsEnabled.value

	override fun setAnalyticsCollectionEnabled(enabled: Boolean) {
		settings.putBoolean(ANALYTICS_COLLECTION_ENABLED_KEY, enabled)
		analyticsEnabled.value = enabled
	}
}

private const val ANALYTICS_COLLECTION_ENABLED_KEY = "analyticsCollectionEnabled"
