package com.gdavidpb.tuindice.base.data.source

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryEvaluationsWeekFocusDataSourceTest {
	private val focus = InMemoryEvaluationsWeekFocusDataSource()

	@Test
	fun nothingIsPendingAtFirst() {
		assertNull(focus.pendingWeekStart.value)
	}

	@Test
	fun aRequestStaysPendingUntilSomeoneConsumesIt() {
		focus.request(MONDAY)

		assertEquals(MONDAY, focus.pendingWeekStart.value)
		assertEquals(MONDAY, focus.pendingWeekStart.value)
	}

	@Test
	fun aRequestIsConsumedByTheFirstCallerOnly() {
		focus.request(MONDAY)

		assertTrue(focus.consume(MONDAY))
		assertFalse(focus.consume(MONDAY))
		assertNull(focus.pendingWeekStart.value)
	}

	@Test
	fun aNewerRequestReplacesTheOneNotYetConsumed_andTheOldOneCannotTakeIt() {
		focus.request(MONDAY)
		focus.request(NEXT_MONDAY)

		assertFalse(focus.consume(MONDAY))
		assertEquals(NEXT_MONDAY, focus.pendingWeekStart.value)
		assertTrue(focus.consume(NEXT_MONDAY))
	}

	@Test
	fun theSameWeekCanBeAskedForAgainOnceConsumed() {
		focus.request(MONDAY)
		focus.consume(MONDAY)
		focus.request(MONDAY)

		assertEquals(MONDAY, focus.pendingWeekStart.value)
	}

	@Test
	fun theEndOfTheSessionDropsWhatWasPending() = runTest {
		focus.request(MONDAY)

		focus.clearSessionMemory()

		assertNull(focus.pendingWeekStart.value)
		assertFalse(focus.consume(MONDAY))
	}

	private companion object {
		val MONDAY = LocalDate(2026, 10, 26)
		val NEXT_MONDAY = LocalDate(2026, 11, 2)
	}
}
