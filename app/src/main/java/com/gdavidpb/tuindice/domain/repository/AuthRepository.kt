package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Account
import java.util.*

interface AuthRepository {
    suspend fun getActiveAccount(lastUpdate: Date): Account?

    suspend fun signIn(email: String, password: String): Account
    suspend fun signUp(email: String, password: String): Account
    suspend fun signOut()

    suspend fun confirmPasswordReset(code: String, password: String)
    suspend fun sendPasswordResetEmail(email: String, password: String)
    suspend fun sendEmailVerification()
    suspend fun isEmailVerified(): Boolean

    suspend fun updateToken(token: String)

    suspend fun isResetLink(link: String?): Boolean
}