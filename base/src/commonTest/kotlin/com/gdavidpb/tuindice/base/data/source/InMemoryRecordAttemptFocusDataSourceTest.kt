package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.domain.model.RecordAttemptFocusRequest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryRecordAttemptFocusDataSourceTest {
	private val focus = InMemoryRecordAttemptFocusDataSource()

	@Test
	fun nothingIsPendingAtFirst() {
		assertNull(focus.pending.value)
	}

	@Test
	fun aRequestStaysPendingUntilSomeoneConsumesIt() {
		focus.request(ATTEMPT_ID)

		assertEquals(RecordAttemptFocusRequest(ATTEMPT_ID), focus.pending.value)
		assertEquals(RecordAttemptFocusRequest(ATTEMPT_ID), focus.pending.value)
	}

	@Test
	fun aRequestIsConsumedByTheFirstCallerOnly() {
		focus.request(ATTEMPT_ID)

		assertTrue(focus.consume(RecordAttemptFocusRequest(ATTEMPT_ID)))
		assertFalse(focus.consume(RecordAttemptFocusRequest(ATTEMPT_ID)))
		assertNull(focus.pending.value)
	}

	@Test
	fun aNewerRequestReplacesTheOneNotYetConsumed_andTheOldOneCannotTakeIt() {
		focus.request(ATTEMPT_ID)
		focus.request(OTHER_ATTEMPT_ID)

		assertFalse(focus.consume(RecordAttemptFocusRequest(ATTEMPT_ID)))
		assertEquals(RecordAttemptFocusRequest(OTHER_ATTEMPT_ID), focus.pending.value)
		assertTrue(focus.consume(RecordAttemptFocusRequest(OTHER_ATTEMPT_ID)))
	}

	@Test
	fun theSameAttemptCanBeAskedForAgainOnceConsumed() {
		focus.request(ATTEMPT_ID)
		focus.consume(RecordAttemptFocusRequest(ATTEMPT_ID))
		focus.request(ATTEMPT_ID)

		assertEquals(RecordAttemptFocusRequest(ATTEMPT_ID), focus.pending.value)
	}

	@Test
	fun theEndOfTheSessionDropsWhatWasPending() = runTest {
		focus.request(ATTEMPT_ID)

		focus.clearSessionMemory()

		assertNull(focus.pending.value)
		assertFalse(focus.consume(RecordAttemptFocusRequest(ATTEMPT_ID)))
	}

	private companion object {
		const val ATTEMPT_ID = "attempt-1"
		const val OTHER_ATTEMPT_ID = "attempt-2"
	}
}
