package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Auth
import com.gdavidpb.tuindice.domain.model.Credentials

interface AuthRepository {
    suspend fun isActiveAuth(): Boolean
    suspend fun getActiveAuth(): Auth
    suspend fun signIn(credentials: Credentials): Auth
    suspend fun signUp(credentials: Credentials): Auth
    suspend fun signOut()

    suspend fun confirmPasswordReset(code: String, password: String)
    suspend fun confirmVerifyEmail(code: String)
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun sendVerificationEmail()
    suspend fun isEmailVerified(): Boolean

    suspend fun isResetPasswordLink(link: String): Boolean
    suspend fun isVerifyEmailLink(link: String): Boolean
}