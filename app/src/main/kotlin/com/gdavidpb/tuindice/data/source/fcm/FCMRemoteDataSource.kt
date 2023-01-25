package com.gdavidpb.tuindice.data.source.fcm

import com.gdavidpb.tuindice.base.utils.extensions.awaitOrNull
import com.gdavidpb.tuindice.data.source.fcm.source.RemoteDataSource
import com.google.firebase.messaging.FirebaseMessaging

class FCMRemoteDataSource(
	private val firebaseMessaging: FirebaseMessaging
) : RemoteDataSource {
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