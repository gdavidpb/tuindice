package com.gdavidpb.tuindice.data.source.messaging

import com.gdavidpb.tuindice.base.data.repository.messaging.PushTokenDataRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FirebasePushTokenDataSource(
	private val firebaseMessaging: FirebaseMessaging
) : PushTokenDataRepository {
	override suspend fun getToken(): String {
		return firebaseMessaging.token.await()
			.takeIf { token -> token.isNotBlank() }
			?: throw IllegalStateException("Push token unavailable from Firebase Messaging.")
	}
}
