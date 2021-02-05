package com.gdavidpb.tuindice.datastores

import com.gdavidpb.tuindice.domain.repository.MessagingRepository
import java.util.*

open class MessagingMockDataStore : MessagingRepository {
    override suspend fun getToken(): String? {
        return UUID.randomUUID().toString()
    }
}