package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Auth

interface AuthRepository {
	suspend fun isActiveAuth(): Boolean
	suspend fun getActiveAuth(): Auth
	suspend fun getActiveToken(): String
	suspend fun getAuthProvider(): String
	suspend fun signIn(token: String): Auth
	suspend fun signOut()
}