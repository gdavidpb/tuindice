package com.gdavidpb.tuindice.datasources

import com.gdavidpb.tuindice.domain.repository.MessagingRepository
import java.util.*

open class MessagingMockDataSource : MessagingRepository {
    override suspend fun getToken(): String? {
        return UUID.randomUUID().toString()
    }

    override suspend fun subscribeToTopic(topic: String) {
    }

    override suspend fun unsubscribeFromTopic(topic: String) {
    }
}