package com.gdavidpb.tuindice.base.data.repository

import com.gdavidpb.tuindice.base.data.source.InMemorySessionDataSource
import com.gdavidpb.tuindice.base.data.source.PreferencesSessionDataSource
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class SessionDataRepositoryTest {
	@Test
	fun getAccessToken_loadsFromPreferencesAndCachesMemory() {
		runBlocking {
			val memory = InMemorySessionDataSource()
			val preferences = FakePreferencesSessionDataSource(
				accessToken = "token-from-preferences"
			)
			val repository = SessionDataRepository(memory, preferences)

			val firstRead = repository.getAccessToken()

			preferences.accessToken = "updated-token"
			val secondRead = repository.getAccessToken()

			assertEquals("token-from-preferences", firstRead)
			assertEquals("token-from-preferences", secondRead)
		}
	}

	@Test
	fun clear_removesSessionFromMemoryAndPreferences() {
		runBlocking {
			val memory = InMemorySessionDataSource()
			val preferences = FakePreferencesSessionDataSource(
				usbId = "20320000",
				accessToken = "access",
				refreshToken = "refresh"
			)
			val repository = SessionDataRepository(memory, preferences)

			repository.getAccessToken()
			repository.setRefreshToken("refresh")
			repository.clear()

			assertFalse(preferences.hasActiveSession())
			assertFailsWith<IllegalStateException> {
				repository.getAccessToken()
			}
			assertFailsWith<IllegalStateException> {
				repository.getRefreshToken()
			}
		}
	}

	@Test
	fun getRefreshToken_throwsWhenSessionDoesNotExist() {
		runBlocking {
			val repository = SessionDataRepository(
				memorySessionDataSource = InMemorySessionDataSource(),
				preferencesSessionDataSource = FakePreferencesSessionDataSource()
			)

			assertFailsWith<IllegalStateException> {
				repository.getRefreshToken()
			}
		}
	}
}

private class FakePreferencesSessionDataSource(
	var usbId: String? = null,
	var accessToken: String? = null,
	var refreshToken: String? = null
) : PreferencesSessionDataSource {
	override suspend fun hasActiveSession(): Boolean {
		return accessToken != null || refreshToken != null
	}

	override suspend fun setUsbId(usbId: String) {
		this.usbId = usbId
	}

	override suspend fun setAccessToken(accessToken: String) {
		this.accessToken = accessToken
	}

	override suspend fun setRefreshToken(refreshToken: String) {
		this.refreshToken = refreshToken
	}

	override suspend fun getUsbId(): String? = usbId

	override suspend fun getAccessToken(): String? = accessToken

	override suspend fun getRefreshToken(): String? = refreshToken

	override suspend fun clear() {
		usbId = null
		accessToken = null
		refreshToken = null
	}
}
