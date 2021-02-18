package com.gdavidpb.tuindice.data.source.token

import com.gdavidpb.tuindice.domain.repository.MessagingRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

open class FirebaseCloudMessagingDataStore(
        private val firebaseMessaging: FirebaseMessaging
) : MessagingRepository {
    override suspend fun getToken(): String? {
        return runCatching {
            firebaseMessaging.token.await()
        }.getOrNull()
    }
}