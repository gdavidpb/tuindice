package com.gdavidpb.tuindice.data.source

import android.content.SharedPreferences
import com.gdavidpb.tuindice.base.domain.model.Auth
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import java.util.*

class AuthMockDataSource(
    private val sharedPreferences: SharedPreferences
) : AuthRepository {

    private val uid = "HukCYRZdCmWhSzfYS0Tl7f0e9Rt6"
    private val email = "11-11111@usb.ve"

    private val token = UUID.randomUUID().toString()

    override suspend fun isActiveAuth(): Boolean {
        return sharedPreferences.all.isNotEmpty()
    }

    override suspend fun getActiveAuth(): Auth {
        return Auth(uid = uid, email = email)
    }

    override suspend fun signIn(token: String): Auth {
        return Auth(uid = uid, email = email)
    }

    override suspend fun signOut() {
    }

    override suspend fun getActiveToken(): String {
        return token
    }

    override suspend fun getAuthProvider(): String {
        return ""
    }
}