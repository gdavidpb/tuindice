package com.gdavidpb.tuindice.login.data.source

import com.gdavidpb.tuindice.login.data.repository.LoginMessagingDataSource
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FirebaseLoginMessagingDataSource(
	private val firebaseMessaging: FirebaseMessaging
) : LoginMessagingDataSource {
	override suspend fun getToken(): String {
		return firebaseMessaging.token.await()
			.takeIf { token -> token.isNotBlank() }
			?: throw IllegalStateException("Push token unavailable from Firebase Messaging.")
	}
}
