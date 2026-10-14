package com.gdavidpb.tuindice.academiccore.domain.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RecordPaceEngineTest {
	@Test
	fun paceOf_averagesTheApprovedCreditsOfTheRegularTerms() {
		val pace = RecordPaceEngine.paceOf(listOf(6, 4, 2), pensumApprovedCredits = null, pensumTotalCredits = null)

		assertEquals(4.0, pace.averageCredits)
	}

	@Test
	fun paceOf_countsARegularTermWithoutApprovedCredits_inTheAverageAndInTheProjection() {
		// Average (6 + 0 + 6) / 3 = 4. 32 - 12 = 20 credits left: 20 / 4 = 5 terms.
		val pace = RecordPaceEngine.paceOf(listOf(6, 0, 6), pensumApprovedCredits = 12, pensumTotalCredits = 32)

		assertEquals(4.0, pace.averageCredits)
		assertEquals(5, pace.remainingTerms)
	}

	@Test
	fun paceOf_roundsAPartialTermUp() {
		// Average 12 / 3 = 4. 22 - 12 = 10 credits left: 10 / 4 = 2.5, so 3 terms.
		val pace = RecordPaceEngine.paceOf(listOf(6, 4, 2), pensumApprovedCredits = 12, pensumTotalCredits = 22)

		assertEquals(3, pace.remainingTerms)
	}

	@Test
	fun paceOf_doesNotAddATermBecauseOfFloatingPoint() {
		// Average 17 / 7. Then 17 / (17 / 7) is exactly 7, but in doubles it is 7.000000000000001, and rounding that up
		// would say 8 terms.
		val pace = RecordPaceEngine.paceOf(
			listOf(3, 3, 2, 3, 2, 2, 2),
			pensumApprovedCredits = 17,
			pensumTotalCredits = 34
		)

		assertEquals(7, pace.remainingTerms)
	}

	@Test
	fun paceOf_withoutThePensumCredits_hasAnAverageButNoProjection() {
		val withoutApproved = RecordPaceEngine.paceOf(listOf(6, 4), pensumApprovedCredits = null, pensumTotalCredits = 40)
		val withoutTotal = RecordPaceEngine.paceOf(listOf(6, 4), pensumApprovedCredits = 10, pensumTotalCredits = null)

		assertEquals(5.0, withoutApproved.averageCredits)
		assertNull(withoutApproved.remainingTerms)
		assertEquals(5.0, withoutTotal.averageCredits)
		assertNull(withoutTotal.remainingTerms)
	}

	@Test
	fun paceOf_withOnlyOneRegularTerm_hasAnAverageButNoProjection() {
		val pace = RecordPaceEngine.paceOf(listOf(6), pensumApprovedCredits = 6, pensumTotalCredits = 40)

		assertEquals(6.0, pace.averageCredits)
		assertNull(pace.remainingTerms)
	}

	@Test
	fun paceOf_withoutRegularTerms_hasNeitherAverageNorProjection() {
		val pace = RecordPaceEngine.paceOf(emptyList(), pensumApprovedCredits = 0, pensumTotalCredits = 40)

		assertNull(pace.averageCredits)
		assertNull(pace.remainingTerms)
	}

	@Test
	fun paceOf_withNothingLeftInThePensum_hasNoProjection() {
		val complete = RecordPaceEngine.paceOf(listOf(6, 4), pensumApprovedCredits = 40, pensumTotalCredits = 40)
		val surplus = RecordPaceEngine.paceOf(listOf(6, 4), pensumApprovedCredits = 50, pensumTotalCredits = 40)

		assertNull(complete.remainingTerms)
		assertNull(surplus.remainingTerms)
	}

	@Test
	fun paceOf_withoutApprovedCreditsInTheRegularTerms_hasNoProjection() {
		val pace = RecordPaceEngine.paceOf(listOf(0, 0), pensumApprovedCredits = 0, pensumTotalCredits = 40)

		assertEquals(0.0, pace.averageCredits)
		assertNull(pace.remainingTerms)
	}
}
