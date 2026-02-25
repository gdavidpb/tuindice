package com.gdavidpb.tuindice.base.data.source

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InMemorySessionDataSourceTest {
	@Test
	fun hasActiveSession_returnsFalseWhenNoTokens() = runBlocking {
		val dataSource = InMemorySessionDataSource()

		assertFalse(dataSource.hasActiveSession())
	}

	@Test
	fun hasActiveSession_returnsTrueWhenOnlyRefreshTokenExists() = runBlocking {
		val dataSource = InMemorySessionDataSource()

		dataSource.setRefreshToken("refresh-token")

		assertTrue(dataSource.hasActiveSession())
	}

	@Test
	fun hasActiveSession_returnsTrueWhenOnlyAccessTokenExists() = runBlocking {
		val dataSource = InMemorySessionDataSource()

		dataSource.setAccessToken("access-token")

		assertTrue(dataSource.hasActiveSession())
	}
}
