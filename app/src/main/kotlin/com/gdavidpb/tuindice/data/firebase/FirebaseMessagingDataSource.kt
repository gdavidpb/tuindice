package com.gdavidpb.tuindice.data.firebase

import com.gdavidpb.tuindice.base.utils.extension.awaitOrNull
import com.gdavidpb.tuindice.data.messaging.source.ProviderDataSource
import com.google.firebase.messaging.FirebaseMessaging

class FirebaseMessagingDataSource(
	private val firebaseMessaging: FirebaseMessaging
) : ProviderDataSource {
	override suspend fun getToken(): String? {
		return firebaseMessaging.token.awaitOrNull()
	}

	override suspend fun subscribeToTopic(topic: String) {
		firebaseMessaging.subscribeToTopic(topic).awaitOrNull()
	}

	override suspend fun unsubscribeFromTopic(topic: String) {
		firebaseMessaging.unsubscribeFromTopic(topic).awaitOrNull()
	}
}