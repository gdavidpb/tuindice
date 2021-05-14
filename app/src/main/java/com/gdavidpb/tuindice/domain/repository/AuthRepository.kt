package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Auth
import com.gdavidpb.tuindice.domain.model.Credentials

interface AuthRepository {
    suspend fun isActiveAuth(): Boolean
    suspend fun getActiveAuth(): Auth
    suspend fun reloadActiveAuth(): Auth
    suspend fun signIn(credentials: Credentials): Auth
    suspend fun reSignIn(credentials: Credentials): Auth
    suspend fun signUp(credentials: Credentials): Auth
    suspend fun signOut()

    suspend fun getToken(forceRefresh: Boolean): String?

    suspend fun updatePassword(newPassword: String)
    suspend fun confirmPasswordReset(code: String, password: String)
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun isResetPasswordLink(link: String): Boolean
}