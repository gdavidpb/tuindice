package com.gdavidpb.tuindice.base.domain.repository

import kotlinx.coroutines.flow.Flow

interface OutdatedCredentialsRepository {
	fun observeOutdatedCredentials(): Flow<Boolean>
	suspend fun hasOutdatedCredentials(): Boolean
	suspend fun setOutdatedCredentials()
	suspend fun clearOutdatedCredentials()
}
