package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Auth

interface AuthRepository {
	suspend fun isActiveAuth(): Boolean
	suspend fun getActiveAuth(): Auth
	suspend fun getActiveToken(): String
	suspend fun auth(token: String): Auth
	suspend fun revoke()
}