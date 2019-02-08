package com.gdavidpb.tuindice.data.source.token

import com.gdavidpb.tuindice.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.utils.await
import com.google.firebase.iid.FirebaseInstanceId

open class TokenDataStore(
        private val firebaseInstanceId: FirebaseInstanceId
) : IdentifierRepository {
    override suspend fun getIdentifier(): String {
        return firebaseInstanceId.instanceId.await().token
    }
}