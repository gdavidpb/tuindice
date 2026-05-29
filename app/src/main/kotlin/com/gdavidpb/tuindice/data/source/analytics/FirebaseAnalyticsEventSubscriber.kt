package com.gdavidpb.tuindice.data.source.analytics

import android.os.Bundle
import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FirebaseAnalyticsEventSubscriber(
	private val firebaseAnalytics: FirebaseAnalytics,
	private val usageDataConsentRepository: UsageDataConsentRepository
) : EventSubscriber {
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

	init {
		firebaseAnalytics.setAnalyticsCollectionEnabled(
			usageDataConsentRepository.isUsageDataCollectionEnabled()
		)
		scope.launch {
			usageDataConsentRepository.usageDataCollectionEnabled.collectLatest { enabled ->
				firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
			}
		}
	}

	override val id: String = "firebase_analytics_android"

	override val isEnabled: Boolean
		get() = usageDataConsentRepository.isUsageDataCollectionEnabled()

	override fun onEvent(event: AppEvent) {
		if (!isEnabled) return

		firebaseAnalytics.logEvent(event.name, event.parameters.toBundle())
	}
}

private fun Map<String, String>.toBundle(): Bundle {
	return Bundle(size).also { bundle ->
		forEach { (key, value) ->
			bundle.putString(key, value)
		}
	}
}
