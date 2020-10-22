package com.gdavidpb.tuindice.datastores

import android.content.SharedPreferences
import com.gdavidpb.tuindice.domain.model.Auth
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.utils.KEY_USB_ID

open class AuthMockDataStore(
        private val sharedPreferences: SharedPreferences
) : AuthRepository {

    private val uid = "HukCYRZdCmWhSzfYS0Tl7f0e9Rt6"

    override suspend fun isActiveAuth(): Boolean {
        return sharedPreferences.contains(KEY_USB_ID)
    }

    override suspend fun getActiveAuth(): Auth {
        val email = sharedPreferences.getString(KEY_USB_ID, null) ?: error("unauthenticated")

        return Auth(uid = uid, email = email)
    }

    override suspend fun signIn(email: String, password: String): Auth {
        return Auth(uid = uid, email = email)
    }

    override suspend fun signUp(email: String, password: String): Auth {
        return Auth(uid = uid, email = email)
    }

    override suspend fun signOut() {
    }

    override suspend fun confirmPasswordReset(code: String, password: String) {
    }

    override suspend fun confirmVerifyEmail(code: String) {
    }

    override suspend fun sendPasswordResetEmail(email: String) {
    }

    override suspend fun sendVerificationEmail() {
    }

    override suspend fun isEmailVerified(): Boolean {
        return true
    }

    override suspend fun isResetPasswordLink(link: String): Boolean {
        return false
    }

    override suspend fun isVerifyEmailLink(link: String): Boolean {
        return false
    }
}