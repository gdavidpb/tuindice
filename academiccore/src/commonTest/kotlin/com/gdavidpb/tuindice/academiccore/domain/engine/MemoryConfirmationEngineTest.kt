package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.memory.MemoryAnchor
import com.gdavidpb.tuindice.academiccore.domain.model.memory.MemoryAnchorKind
import com.gdavidpb.tuindice.academiccore.domain.model.memory.MemoryCategory
import com.gdavidpb.tuindice.academiccore.domain.model.memory.MemoryState
import com.gdavidpb.tuindice.academiccore.domain.model.memory.MemoryValidityInput
import com.gdavidpb.tuindice.academiccore.domain.model.memory.MemoryValiditySnapshot
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class MemoryConfirmationEngineTest {
	private val today = LocalDate(2026, 10, 16)

	private val snapshot = MemoryValiditySnapshot(
		today = today,
		careerCode = "ICI",
		activeTermKeys = setOf("2026-2"),
		resolvedAttemptIds = emptySet(),
		attemptTermKeys = emptyMap(),
		resolvedEvaluationIds = emptySet()
	)

	private fun factOf(
		category: MemoryCategory,
		anchor: MemoryAnchor,
		snapshot: MemoryValiditySnapshot = this.snapshot
	): String? {
		val input = MemoryValidityInput(category, anchor, MemoryState.ACTIVE, lastUsedOn = null, pendingSince = null)

		return MemoryConfirmationEngine.confirmationFactOf(input, snapshot)
	}

	@Test
	fun anInterestAnchoredToTheCareerReadsTheCareer() {
		assertEquals("career:ICI", factOf(MemoryCategory.INTEREST, MemoryAnchor(MemoryAnchorKind.CAREER, "ICI", null)))
	}

	@Test
	fun aCareerGoalReadsTheCareerAndWhetherItsDatePassed() {
		assertEquals(
			"career:ICI;datePassed:false",
			factOf(MemoryCategory.CAREER_GOAL, MemoryAnchor(MemoryAnchorKind.CAREER, "ICI", LocalDate(2026, 12, 1)))
		)
		assertEquals(
			"career:ICI;datePassed:true",
			factOf(MemoryCategory.CAREER_GOAL, MemoryAnchor(MemoryAnchorKind.CAREER, "ICI", LocalDate(2026, 10, 1)))
		)
	}

	@Test
	fun aGoalWithoutDateReadsThatNoDatePassed() {
		assertEquals(
			"career:ICI;datePassed:false",
			factOf(MemoryCategory.CAREER_GOAL, MemoryAnchor(MemoryAnchorKind.CAREER, "ICI", null))
		)
	}

	@Test
	fun aTermReadsWhetherTheTermIsStillActive() {
		assertEquals("termActive:true", factOf(MemoryCategory.TERM, MemoryAnchor(MemoryAnchorKind.TERM, "2026-2", null)))
		assertEquals("termActive:false", factOf(MemoryCategory.TERM, MemoryAnchor(MemoryAnchorKind.TERM, "2026-1", null)))
	}

	@Test
	fun aTermWithoutReferenceReadsAsInactive() {
		assertEquals("termActive:false", factOf(MemoryCategory.TERM, MemoryAnchor(MemoryAnchorKind.TERM, null, null)))
	}

	@Test
	fun theRulesThatNeverAskHaveNoFact() {
		val none = MemoryAnchor(MemoryAnchorKind.NONE, null, null)
		val attempt = MemoryAnchor(MemoryAnchorKind.ATTEMPT, "attempt-1", null)
		val date = MemoryAnchor(MemoryAnchorKind.DATE, null, LocalDate(2026, 11, 1))

		assertNull(factOf(MemoryCategory.PREFERENCE, none))
		assertNull(factOf(MemoryCategory.SUBJECT, attempt))
		assertNull(factOf(MemoryCategory.DATED_RESTRICTION, date))
	}

	@Test
	fun theFactFollowsTheSnapshotItIsReadFrom() {
		val anchor = MemoryAnchor(MemoryAnchorKind.CAREER, "ICI", null)
		val changed = snapshot.copy(careerCode = "ISC")

		assertNotEquals(
			factOf(MemoryCategory.INTEREST, anchor, snapshot),
			factOf(MemoryCategory.INTEREST, anchor, changed)
		)
	}
}
