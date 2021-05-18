package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Auth

interface AuthRepository {
    suspend fun isActiveAuth(): Boolean
    suspend fun getActiveAuth(): Auth
    suspend fun getActiveToken(forceRefresh: Boolean): String
    suspend fun reloadActiveAuth(): Auth
    suspend fun signIn(token: String): Auth
    suspend fun signOut()
}