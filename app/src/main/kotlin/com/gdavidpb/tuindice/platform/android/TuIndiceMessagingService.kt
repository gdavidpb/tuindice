package com.gdavidpb.tuindice.platform.android

import com.google.firebase.messaging.FirebaseMessagingService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TuIndiceMessagingService : FirebaseMessagingService(), KoinComponent {
	private val pushTokenRotationHandler: PushTokenRotationHandler by inject()

	override fun onNewToken(token: String) {
		pushTokenRotationHandler.onPushTokenRotated()
	}
}
