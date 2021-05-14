package com.gdavidpb.tuindice.datasources

import com.gdavidpb.tuindice.domain.repository.MessagingRepository
import java.util.*

open class MessagingMockDataSource : MessagingRepository {

    private val token =  UUID.randomUUID().toString()

    override suspend fun getToken(): String? {
        return token
    }

    override suspend fun subscribeToTopic(topic: String) {
    }

    override suspend fun unsubscribeFromTopic(topic: String) {
    }
}