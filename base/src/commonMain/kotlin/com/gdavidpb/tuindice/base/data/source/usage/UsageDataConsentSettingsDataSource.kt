package com.gdavidpb.tuindice.base.data.source.usage

import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UsageDataConsentSettingsDataSource(
	private val settings: Settings
) : UsageDataConsentRepository {
	private val usageDataEnabled = MutableStateFlow(
		settings.getBooleanOrNull(USAGE_DATA_COLLECTION_ENABLED_KEY)
			?: settings.getBooleanOrNull(LEGACY_ANALYTICS_COLLECTION_ENABLED_KEY)
			?: false
	)

	override val usageDataCollectionEnabled: StateFlow<Boolean> =
		usageDataEnabled.asStateFlow()

	override fun isUsageDataCollectionEnabled(): Boolean =
		usageDataEnabled.value

	override fun setUsageDataCollectionEnabled(enabled: Boolean) {
		settings.putBoolean(USAGE_DATA_COLLECTION_ENABLED_KEY, enabled)
		usageDataEnabled.value = enabled
	}
}

private const val USAGE_DATA_COLLECTION_ENABLED_KEY = "usageDataCollectionEnabled"
private const val LEGACY_ANALYTICS_COLLECTION_ENABLED_KEY = "analyticsCollectionEnabled"
