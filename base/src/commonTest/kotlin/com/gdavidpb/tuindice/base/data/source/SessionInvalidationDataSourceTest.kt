package com.gdavidpb.tuindice.base.data.source

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SessionInvalidationDataSourceTest {
	@Test
	fun invalidationEmittedWhileNobodyCollectsIsDeliveredOnNextCollect() = runTest {
		val dataSource = SessionInvalidationDataSource()

		dataSource.notifySessionInvalidated(sessionId = "session-1")

		val received = withTimeout(1_000) {
			dataSource.observeSessionInvalidation().first()
		}

		assertEquals(Unit, received)
	}

	@Test
	fun intentionalSignOutSuppressesOnlyItsOwnInvalidation() = runTest {
		val dataSource = SessionInvalidationDataSource()

		dataSource.markIntentionalSignOut(sessionId = "session-1")
		dataSource.notifySessionInvalidated(sessionId = "session-1")

		val suppressed = withTimeoutOrNull(1_000) {
			dataSource.observeSessionInvalidation().first()
		}

		assertNull(suppressed)

		dataSource.notifySessionInvalidated(sessionId = null)

		val delivered = withTimeoutOrNull(1_000) {
			dataSource.observeSessionInvalidation().first()
		}

		assertNotNull(delivered)
	}

	@Test
	fun blankSessionInvalidationIsDeliveredWithoutMarker() = runTest {
		val dataSource = SessionInvalidationDataSource()

		dataSource.notifySessionInvalidated(sessionId = null)

		val received = withTimeoutOrNull(1_000) {
			dataSource.observeSessionInvalidation().first()
		}

		assertNotNull(received)
	}
}
