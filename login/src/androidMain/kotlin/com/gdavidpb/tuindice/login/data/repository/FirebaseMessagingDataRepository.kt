package com.gdavidpb.tuindice.login.data.repository

import com.gdavidpb.tuindice.login.domain.repository.MessagingRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FirebaseMessagingDataRepository(
	private val firebaseMessaging: FirebaseMessaging
) : MessagingRepository {
	override suspend fun getToken(): String {
		return firebaseMessaging.token.await()
			.takeIf { token -> token.isNotBlank() }
			?: throw IllegalStateException("Push token unavailable from Firebase Messaging.")
	}
}
