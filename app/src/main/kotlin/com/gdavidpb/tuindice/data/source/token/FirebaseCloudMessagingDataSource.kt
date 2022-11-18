package com.gdavidpb.tuindice.data.source.token

import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.utils.extensions.awaitOrNull
import com.google.firebase.messaging.FirebaseMessaging

class FirebaseCloudMessagingDataSource(
    private val firebaseMessaging: FirebaseMessaging
) : MessagingRepository {
    override suspend fun getToken(): String? {
        return firebaseMessaging.token.awaitOrNull()
    }

    override suspend fun subscribeToTopic(topic: String) {
        firebaseMessaging.subscribeToTopic(topic).awaitOrNull()
    }

    override suspend fun unsubscribeFromTopic(topic: String) {
        firebaseMessaging.unsubscribeFromTopic(topic)
    }
}