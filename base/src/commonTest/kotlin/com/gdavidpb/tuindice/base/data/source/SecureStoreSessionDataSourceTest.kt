package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SecureStoreSessionDataSourceTest {
	@Test
	fun hasActiveSession_usesAccessOrRefreshTokenPresence() = runBlocking {
		val secureStore = FakeSecureStoreDataSource()
		val dataSource = SecureStoreSessionDataSource(secureStore)

		assertFalse(dataSource.hasActiveSession())

		secureStore.putString(PreferencesKeys.USER_REFRESH_TOKEN, "refresh")
		assertTrue(dataSource.hasActiveSession())

		secureStore.clear()
		secureStore.putString(PreferencesKeys.USER_ACCESS_TOKEN, "access")
		assertTrue(dataSource.hasActiveSession())
	}

	@Test
	fun clear_removesAllPersistedSessionValues() = runBlocking {
		val secureStore = FakeSecureStoreDataSource()
		val dataSource = SecureStoreSessionDataSource(secureStore)

		dataSource.setUsbId("20320000")
		dataSource.setAccessToken("access")
		dataSource.setRefreshToken("refresh")

		assertEquals("20320000", dataSource.getUsbId())
		assertEquals("access", dataSource.getAccessToken())
		assertEquals("refresh", dataSource.getRefreshToken())

		dataSource.clear()

		assertNull(dataSource.getUsbId())
		assertNull(dataSource.getAccessToken())
		assertNull(dataSource.getRefreshToken())
		assertFalse(dataSource.hasActiveSession())
	}
}

private class FakeSecureStoreDataSource : SecureStoreDataSource {
	private val values = mutableMapOf<String, String>()

	override fun contains(key: String): Boolean = values.containsKey(key)

	override fun getString(key: String): String? = values[key]

	override fun putString(key: String, value: String) {
		values[key] = value
	}

	override fun clear() {
		values.clear()
	}
}
