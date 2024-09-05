package com.gdavidpb.tuindice.data.repository.messaging.source

import com.gdavidpb.tuindice.data.repository.messaging.ProviderDataSource
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FirebaseMessagingDataSource(
	private val firebaseMessaging: FirebaseMessaging
) : ProviderDataSource {
	override suspend fun getToken(): String? {
		return runCatching { firebaseMessaging.token.await() }.getOrNull()
	}
}