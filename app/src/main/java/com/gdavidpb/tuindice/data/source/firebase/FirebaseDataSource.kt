package com.gdavidpb.tuindice.data.source.firebase

import com.gdavidpb.tuindice.domain.model.Auth
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.utils.extensions.awaitOrNull
import com.gdavidpb.tuindice.utils.mappers.toAuth
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

open class FirebaseDataSource(
        private val auth: FirebaseAuth
) : AuthRepository {
    override suspend fun isActiveAuth(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun getActiveAuth(): Auth {
        return auth.currentUser
            ?.toAuth()
            ?: error("getActiveAuth")
    }

    override suspend fun getActiveToken(forceRefresh: Boolean): String {
        return auth.currentUser
            ?.getIdToken(forceRefresh)
            ?.awaitOrNull()
            ?.token
            ?: error("getToken")
    }

    override suspend fun reloadActiveAuth(): Auth {
        return auth.currentUser
            ?.apply { reload() }
            ?.toAuth()
            ?: error("reloadActiveAuth")
    }

    override suspend fun signIn(token: String): Auth {
        return auth.signInWithCustomToken(token)
            .await()
            .user
            ?.toAuth()
            ?: error("signIn")
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}