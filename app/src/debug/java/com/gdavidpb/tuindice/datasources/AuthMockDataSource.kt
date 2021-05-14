package com.gdavidpb.tuindice.datasources

import android.content.SharedPreferences
import com.gdavidpb.tuindice.domain.model.Auth
import com.gdavidpb.tuindice.domain.model.Credentials
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.utils.SettingsKeys
import java.util.*

open class AuthMockDataSource(
        private val sharedPreferences: SharedPreferences
) : AuthRepository {

    private val uid = "HukCYRZdCmWhSzfYS0Tl7f0e9Rt6"
    private val email = "11-11111@usb.ve"

    private val token =  UUID.randomUUID().toString()

    override suspend fun isActiveAuth(): Boolean {
        return sharedPreferences.contains(SettingsKeys.USB_ID)
    }

    override suspend fun getActiveAuth(): Auth {
        return Auth(uid = uid, email = email)
    }

    override suspend fun reloadActiveAuth(): Auth {
        return Auth(uid = uid, email = email)
    }

    override suspend fun signIn(credentials: Credentials): Auth {
        return Auth(uid = uid, email = email)
    }

    override suspend fun reSignIn(credentials: Credentials): Auth {
        return Auth(uid = uid, email = email)
    }

    override suspend fun signUp(credentials: Credentials): Auth {
        return Auth(uid = uid, email = email)
    }

    override suspend fun signOut() {
    }

    override suspend fun getToken(forceRefresh: Boolean): String {
        return token
    }

    override suspend fun updatePassword(newPassword: String) {
    }

    override suspend fun confirmPasswordReset(code: String, password: String) {
    }

    override suspend fun sendPasswordResetEmail(email: String) {
    }

    override suspend fun isResetPasswordLink(link: String): Boolean {
        return false
    }
}