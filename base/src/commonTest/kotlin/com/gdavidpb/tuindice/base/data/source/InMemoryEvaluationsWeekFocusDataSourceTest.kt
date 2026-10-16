package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.domain.model.EvaluationsWeekFocusRequest
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
		assertNull(focus.pending.value)
	}

	@Test
	fun aRequestStaysPendingUntilSomeoneConsumesIt() {
		focus.request(MONDAY)

		assertEquals(EvaluationsWeekFocusRequest(MONDAY), focus.pending.value)
		assertEquals(EvaluationsWeekFocusRequest(MONDAY), focus.pending.value)
	}

	@Test
	fun aRequestCarriesTheEvaluationToBringIntoView() {
		focus.request(MONDAY, evaluationId = EVALUATION_ID)

		assertEquals(EvaluationsWeekFocusRequest(MONDAY, EVALUATION_ID), focus.pending.value)
	}

	@Test
	fun aRequestIsConsumedByTheFirstCallerOnly() {
		focus.request(MONDAY, evaluationId = EVALUATION_ID)

		assertTrue(focus.consume(EvaluationsWeekFocusRequest(MONDAY, EVALUATION_ID)))
		assertFalse(focus.consume(EvaluationsWeekFocusRequest(MONDAY, EVALUATION_ID)))
		assertNull(focus.pending.value)
	}

	@Test
	fun aNewerRequestReplacesTheOneNotYetConsumed_andTheOldOneCannotTakeIt() {
		focus.request(MONDAY)
		focus.request(NEXT_MONDAY)

		assertFalse(focus.consume(EvaluationsWeekFocusRequest(MONDAY)))
		assertEquals(EvaluationsWeekFocusRequest(NEXT_MONDAY), focus.pending.value)
		assertTrue(focus.consume(EvaluationsWeekFocusRequest(NEXT_MONDAY)))
	}

	@Test
	fun aNewerRequestForTheSameWeekDoesNotLetTheOlderOneBeTaken() {
		focus.request(MONDAY, evaluationId = EVALUATION_ID)
		focus.request(MONDAY, evaluationId = OTHER_EVALUATION_ID)

		assertFalse(focus.consume(EvaluationsWeekFocusRequest(MONDAY, EVALUATION_ID)))
		assertEquals(EvaluationsWeekFocusRequest(MONDAY, OTHER_EVALUATION_ID), focus.pending.value)
	}

	@Test
	fun theSameWeekCanBeAskedForAgainOnceConsumed() {
		focus.request(MONDAY)
		focus.consume(EvaluationsWeekFocusRequest(MONDAY))
		focus.request(MONDAY)

		assertEquals(EvaluationsWeekFocusRequest(MONDAY), focus.pending.value)
	}

	@Test
	fun theEndOfTheSessionDropsWhatWasPending() = runTest {
		focus.request(MONDAY)

		focus.clearSessionMemory()

		assertNull(focus.pending.value)
		assertFalse(focus.consume(EvaluationsWeekFocusRequest(MONDAY)))
	}

	private companion object {
		val MONDAY = LocalDate(2026, 10, 26)
		val NEXT_MONDAY = LocalDate(2026, 11, 2)
		const val EVALUATION_ID = "evaluation-1"
		const val OTHER_EVALUATION_ID = "evaluation-2"
	}
}
