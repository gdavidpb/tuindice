package com.gdavidpb.tuindice.base.data.source.usage

import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UsageDataConsentSettingsDataSource(
	private val settings: Settings
) : UsageDataConsentRepository, SessionMemory {
	private val usageDataEnabled = MutableStateFlow(readStoredConsent())

	override val usageDataCollectionEnabled: StateFlow<Boolean> =
		usageDataEnabled.asStateFlow()

	override fun isUsageDataCollectionEnabled(): Boolean =
		usageDataEnabled.value

	override fun setUsageDataCollectionEnabled(enabled: Boolean) {
		settings.putBoolean(USAGE_DATA_COLLECTION_ENABLED_KEY, enabled)
		usageDataEnabled.value = enabled
	}

	// The consent is wiped with the rest of the stored data; the mirror follows it, so collection
	// stops with the session instead of running on until the process dies.
	override suspend fun clearSessionMemory() {
		usageDataEnabled.value = readStoredConsent()
	}

	private fun readStoredConsent(): Boolean {
		return settings.getBooleanOrNull(USAGE_DATA_COLLECTION_ENABLED_KEY)
			?: settings.getBooleanOrNull(LEGACY_ANALYTICS_COLLECTION_ENABLED_KEY)
			?: false
	}
}

private const val USAGE_DATA_COLLECTION_ENABLED_KEY = "usageDataCollectionEnabled"
private const val LEGACY_ANALYTICS_COLLECTION_ENABLED_KEY = "analyticsCollectionEnabled"
