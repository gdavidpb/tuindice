package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.data.repository.MemorySessionDataRepository
import com.gdavidpb.tuindice.base.data.repository.PreferencesSessionDataRepository
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class SessionDataSourceTest {
	@Test
	fun setSessionSnapshot_persistsTheCompleteSessionState() = runTest {
		val memorySessionDataSource = FakeMemorySessionDataRepository()
		val preferencesSessionDataSource = FakePreferencesSessionDataRepository()
		val dataSource = SessionDataSource(
			memorySessionDataSource = memorySessionDataSource,
			preferencesSessionDataSource = preferencesSessionDataSource
		)
		val snapshot = SessionSnapshot(
			sessionId = "session-1",
			accessToken = "access-1",
			refreshToken = "refresh-1",
			usbId = "12-34567"
		)

		dataSource.setSessionSnapshot(snapshot)

		assertEquals(snapshot, dataSource.getActiveSessionSnapshot())
		assertEquals("12-34567", memorySessionDataSource.usbId)
		assertEquals("session-1", memorySessionDataSource.sessionId)
		assertEquals("access-1", memorySessionDataSource.accessToken)
		assertEquals("refresh-1", memorySessionDataSource.refreshToken)
		assertEquals("session-1", preferencesSessionDataSource.sessionId)
		assertEquals("access-1", preferencesSessionDataSource.accessToken)
		assertEquals("refresh-1", preferencesSessionDataSource.refreshToken)
		assertEquals("12-34567", preferencesSessionDataSource.usbId)
	}

	@Test
	fun getActiveSessionSnapshot_warmsMemoryFromPreferences() = runTest {
		val memorySessionDataSource = FakeMemorySessionDataRepository()
		val preferencesSessionDataSource = FakePreferencesSessionDataRepository(
			sessionId = "session-1",
			accessToken = "access-1",
			refreshToken = "refresh-1",
			usbId = "12-34567"
		)
		val dataSource = SessionDataSource(
			memorySessionDataSource = memorySessionDataSource,
			preferencesSessionDataSource = preferencesSessionDataSource
		)
		val expectedSnapshot = SessionSnapshot(
			sessionId = "session-1",
			accessToken = "access-1",
			refreshToken = "refresh-1",
			usbId = "12-34567"
		)

		assertEquals(expectedSnapshot, dataSource.getActiveSessionSnapshot())
		assertEquals("12-34567", memorySessionDataSource.usbId)
		assertEquals("session-1", memorySessionDataSource.sessionId)
		assertEquals("access-1", memorySessionDataSource.accessToken)
		assertEquals("refresh-1", memorySessionDataSource.refreshToken)
	}

	@Test
	fun getActiveSessionSnapshot_readsCompleteMemorySessionWithoutPreferencesUsbId() = runTest {
		val expectedSnapshot = SessionSnapshot(
			sessionId = "session-1",
			accessToken = "access-1",
			refreshToken = "refresh-1",
			usbId = "12-34567"
		)
		val dataSource = SessionDataSource(
			memorySessionDataSource = FakeMemorySessionDataRepository(
				usbId = expectedSnapshot.usbId,
				sessionId = expectedSnapshot.sessionId,
				accessToken = expectedSnapshot.accessToken,
				refreshToken = expectedSnapshot.refreshToken
			),
			preferencesSessionDataSource = FakePreferencesSessionDataRepository()
		)

		assertEquals(expectedSnapshot, dataSource.getActiveSessionSnapshot())
	}

	@Test
	fun replaceSessionSnapshotIfCurrent_replacesOnlyMatchingSnapshot() = runTest {
		val originalSnapshot = SessionSnapshot(
			sessionId = "session-1",
			accessToken = "access-1",
			refreshToken = "refresh-1",
			usbId = "12-34567"
		)
		val newSnapshot = SessionSnapshot(
			sessionId = "session-2",
			accessToken = "access-2",
			refreshToken = "refresh-2",
			usbId = "12-34567"
		)
		val staleSnapshot = originalSnapshot.copy(accessToken = "stale-access")
		val dataSource = SessionDataSource(
			memorySessionDataSource = FakeMemorySessionDataRepository(),
			preferencesSessionDataSource = FakePreferencesSessionDataRepository()
		)
		dataSource.setSessionSnapshot(originalSnapshot)

		assertEquals(false, dataSource.replaceSessionSnapshotIfCurrent(staleSnapshot, newSnapshot))
		assertEquals(originalSnapshot, dataSource.getActiveSessionSnapshot())
		assertEquals(true, dataSource.replaceSessionSnapshotIfCurrent(originalSnapshot, newSnapshot))
		assertEquals(newSnapshot, dataSource.getActiveSessionSnapshot())
	}
}

private class FakeMemorySessionDataRepository(
	var usbId: String? = null,
	var sessionId: String? = null,
	var accessToken: String? = null,
	var refreshToken: String? = null
) : MemorySessionDataRepository {
	override suspend fun hasActiveSession(): Boolean {
		return usbId != null && sessionId != null && accessToken != null && refreshToken != null
	}

	override suspend fun setUsbId(usbId: String) {
		this.usbId = usbId
	}

	override suspend fun setSessionId(sessionId: String) {
		this.sessionId = sessionId
	}

	override suspend fun setAccessToken(accessToken: String) {
		this.accessToken = accessToken
	}

	override suspend fun setRefreshToken(refreshToken: String) {
		this.refreshToken = refreshToken
	}

	override suspend fun getUsbId(): String? = usbId

	override suspend fun getSessionId(): String? = sessionId

	override suspend fun getAccessToken(): String? = accessToken

	override suspend fun getRefreshToken(): String? = refreshToken

	override suspend fun clear() {
		usbId = null
		sessionId = null
		accessToken = null
		refreshToken = null
	}
}

private class FakePreferencesSessionDataRepository(
	var usbId: String? = null,
	var sessionId: String? = null,
	var accessToken: String? = null,
	var refreshToken: String? = null
) : PreferencesSessionDataRepository {
	override suspend fun hasActiveSession(): Boolean {
		return sessionId != null && accessToken != null && refreshToken != null
	}

	override suspend fun setUsbId(usbId: String) {
		this.usbId = usbId
	}

	override suspend fun setSessionId(sessionId: String) {
		this.sessionId = sessionId
	}

	override suspend fun setAccessToken(accessToken: String) {
		this.accessToken = accessToken
	}

	override suspend fun setRefreshToken(refreshToken: String) {
		this.refreshToken = refreshToken
	}

	override suspend fun getUsbId(): String? = usbId

	override suspend fun getSessionId(): String? = sessionId

	override suspend fun getAccessToken(): String? = accessToken

	override suspend fun getRefreshToken(): String? = refreshToken

	override suspend fun clear() {
		usbId = null
		sessionId = null
		accessToken = null
		refreshToken = null
	}
}
