package com.gdavidpb.tuindice.datastores

import com.gdavidpb.tuindice.domain.repository.IdentifierRepository
import java.util.*

open class TokenMockDataStore : IdentifierRepository {
    override suspend fun getIdentifier(): String? {
        return UUID.randomUUID().toString()
    }
}