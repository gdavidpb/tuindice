package com.gdavidpb.tuindice.data.source.token

import com.gdavidpb.tuindice.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.utils.extensions.awaitCatching
import com.google.firebase.messaging.FirebaseMessaging

open class FirebaseCloudMessagingDataSource(
        private val firebaseMessaging: FirebaseMessaging
) : MessagingRepository {
    override suspend fun getToken(): String? {
        return firebaseMessaging.token.awaitCatching()
    }

    override suspend fun subscribeToTopic(topic: String) {
        firebaseMessaging.subscribeToTopic(topic).awaitCatching()
    }

    override suspend fun unsubscribeFromTopic(topic: String) {
        firebaseMessaging.unsubscribeFromTopic(topic)
    }
}