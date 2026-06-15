package com.gdavidpb.tuindice.data.source.analytics

import android.os.Bundle
import com.gdavidpb.tuindice.base.data.source.event.isAnalyticsRelevant
import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsEventSubscriber(
	private val firebaseAnalytics: FirebaseAnalytics
) : EventSubscriber {
	override val id: String = "firebase_analytics_android"

	override val isEnabled: Boolean = true

	override fun onEvent(event: AppEvent) {
		if (!event.isAnalyticsRelevant()) return

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
