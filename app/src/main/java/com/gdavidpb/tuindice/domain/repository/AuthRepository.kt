package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Auth

interface AuthRepository {
    suspend fun isActiveAuth(): Boolean
    suspend fun getActiveAuth(): Auth
    suspend fun signIn(email: String, password: String): Auth
    suspend fun signUp(email: String, password: String): Auth
    suspend fun signOut()

    suspend fun confirmPasswordReset(code: String, password: String)
    suspend fun sendPasswordResetEmail(email: String, password: String)
    suspend fun sendVerificationEmail()
    suspend fun isEmailVerified(): Boolean

    suspend fun isResetLink(link: String?): Boolean
}