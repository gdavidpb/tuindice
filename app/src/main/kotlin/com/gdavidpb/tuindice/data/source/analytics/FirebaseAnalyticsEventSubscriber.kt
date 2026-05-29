package com.gdavidpb.tuindice.data.source.analytics

import android.os.Bundle
import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.AnalyticsConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FirebaseAnalyticsEventSubscriber(
	private val firebaseAnalytics: FirebaseAnalytics,
	private val analyticsConsentRepository: AnalyticsConsentRepository
) : EventSubscriber {
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

	init {
		firebaseAnalytics.setAnalyticsCollectionEnabled(
			analyticsConsentRepository.isAnalyticsCollectionEnabled()
		)
		scope.launch {
			analyticsConsentRepository.analyticsCollectionEnabled.collectLatest { enabled ->
				firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
			}
		}
	}

	override val id: String = "firebase_analytics_android"

	override val isEnabled: Boolean
		get() = analyticsConsentRepository.isAnalyticsCollectionEnabled()

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
