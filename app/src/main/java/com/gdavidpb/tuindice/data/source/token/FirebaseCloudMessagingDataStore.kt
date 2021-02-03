package com.gdavidpb.tuindice.data.source.token

import com.gdavidpb.tuindice.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.utils.extensions.await
import com.google.firebase.messaging.FirebaseMessaging

open class FirebaseCloudMessagingDataStore(
        private val firebaseMessaging: FirebaseMessaging
) : MessagingRepository {
    override suspend fun getToken(): String? {
        return runCatching {
            firebaseMessaging.token.await()
        }.getOrNull()
    }
}