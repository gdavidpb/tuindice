package com.gdavidpb.tuindice.login.data.source

import com.gdavidpb.tuindice.login.data.repository.MessagingDataSource
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FirebaseMessagingDataSource(
	private val firebaseMessaging: FirebaseMessaging
) : MessagingDataSource {
	override suspend fun getToken(): String {
		return firebaseMessaging.token.await()
	}
}