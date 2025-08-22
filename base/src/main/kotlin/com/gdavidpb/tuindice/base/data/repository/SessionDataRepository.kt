package com.gdavidpb.tuindice.base.data.repository

import com.gdavidpb.tuindice.base.data.source.MemorySessionDataSource
import com.gdavidpb.tuindice.base.data.source.PreferencesSessionDataSource
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository

class SessionDataRepository(
	private val memorySessionDataSource: MemorySessionDataSource,
	private val preferencesSessionDataSource: PreferencesSessionDataSource
) : SessionRepository {
	override suspend fun hasActiveSession(): Boolean {
		return memorySessionDataSource.hasActiveSession() ||
				preferencesSessionDataSource.hasActiveSession()
	}

	override suspend fun setUsbId(usbId: String) {
		preferencesSessionDataSource.setUsbId(usbId)
	}

	override suspend fun setAccessToken(accessToken: String) {
		memorySessionDataSource.setAccessToken(accessToken)
		preferencesSessionDataSource.setAccessToken(accessToken)
	}

	override suspend fun setRefreshToken(refreshToken: String) {
		memorySessionDataSource.setRefreshToken(refreshToken)
		preferencesSessionDataSource.setRefreshToken(refreshToken)
	}

	override suspend fun getUsbId(): String {
		return preferencesSessionDataSource.getUsbId() ?: throw IllegalStateException()
	}

	override suspend fun getAccessToken(): String {
		val memoryAccessToken = memorySessionDataSource.getAccessToken()

		if (memoryAccessToken != null) return memoryAccessToken

		val preferencesAccessToken = preferencesSessionDataSource.getAccessToken()

		if (preferencesAccessToken != null) {
			memorySessionDataSource.setAccessToken(preferencesAccessToken)

			return preferencesAccessToken
		}

		throw IllegalStateException()
	}

	override suspend fun getRefreshToken(): String {
		val memoryRefreshToken = memorySessionDataSource.getRefreshToken()

		if (memoryRefreshToken != null) return memoryRefreshToken

		val preferencesRefreshToken = preferencesSessionDataSource.getRefreshToken()

		if (preferencesRefreshToken != null) {
			memorySessionDataSource.setAccessToken(preferencesRefreshToken)

			return preferencesRefreshToken
		}

		throw IllegalStateException()
	}

	override suspend fun clear() {
		memorySessionDataSource.clear()
		preferencesSessionDataSource.clear()
	}
}