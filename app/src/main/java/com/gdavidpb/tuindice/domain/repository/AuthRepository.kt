package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Account

interface AuthRepository {
    suspend fun signUp(email: String, password: String): Account
    suspend fun signIn(email: String, password: String): Account
    suspend fun signOut()

    suspend fun confirmPasswordReset(code: String, password: String)
    suspend fun sendPasswordResetEmail(email: String, password: String)
    suspend fun sendEmailVerification()
    suspend fun isEmailVerified(): Boolean
    suspend fun isSignedIn(): Boolean

    suspend fun isResetLink(link: String?): Boolean
    suspend fun isVerifyLink(link: String?): Boolean
}