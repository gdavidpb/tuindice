package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.memory.MemoryAnchor
import com.gdavidpb.tuindice.academiccore.domain.model.memory.MemoryAnchorKind
import com.gdavidpb.tuindice.academiccore.domain.model.memory.MemoryCategory
import com.gdavidpb.tuindice.academiccore.domain.model.memory.MemoryGoalOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.memory.MemoryState
import com.gdavidpb.tuindice.academiccore.domain.model.memory.MemoryValidityDecision
import com.gdavidpb.tuindice.academiccore.domain.model.memory.MemoryValidityInput
import com.gdavidpb.tuindice.academiccore.domain.model.memory.MemoryValidityReason
import com.gdavidpb.tuindice.academiccore.domain.model.memory.MemoryValiditySnapshot
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class MemoryValidityEngineTest {
	private val today = LocalDate(2026, 10, 16)

	private val snapshot = MemoryValiditySnapshot(
		today = today,
		careerCode = "ICI",
		activeTermKeys = setOf("2026-2"),
		resolvedAttemptIds = setOf("attempt-passed"),
		attemptTermKeys = mapOf("attempt-open" to "2026-2", "attempt-closed" to "2026-1"),
		resolvedEvaluationIds = setOf("evaluation-graded")
	)

	private val noAnchor = MemoryAnchor(MemoryAnchorKind.NONE, null, null)

	private fun judge(input: MemoryValidityInput) = MemoryValidityEngine.decide(input, snapshot)

	private fun active(
		category: MemoryCategory,
		anchor: MemoryAnchor = noAnchor,
		lastUsedOn: LocalDate? = null,
		goalOutcome: MemoryGoalOutcome = MemoryGoalOutcome.OPEN
	) = judge(MemoryValidityInput(category, anchor, MemoryState.ACTIVE, lastUsedOn, null, goalOutcome))

	private fun pending(category: MemoryCategory, anchor: MemoryAnchor, pendingSince: LocalDate?) = judge(
		MemoryValidityInput(category, anchor, MemoryState.PENDING_CONFIRMATION, null, pendingSince)
	)

	@Test
	fun aPendingMemoryIsArchivedOnlyAfterThirtyDaysWithoutAnAnswer() {
		assertEquals(
			MemoryValidityDecision.Keep,
			pending(MemoryCategory.PREFERENCE, noAnchor, LocalDate(2026, 9, 16))
		)
		assertEquals(
			MemoryValidityDecision.Archive(MemoryValidityReason.UNCONFIRMED),
			pending(MemoryCategory.PREFERENCE, noAnchor, LocalDate(2026, 9, 15))
		)
	}

	@Test
	fun aPendingMemoryWithoutItsPendingDateIsKept() {
		assertEquals(MemoryValidityDecision.Keep, pending(MemoryCategory.PREFERENCE, noAnchor, null))
	}

	@Test
	fun aPendingMemoryWaitsForItsTimeoutWhateverItsCategory() {
		// Its term is closed, but a pending memory is only ever archived by the timeout.
		assertEquals(
			MemoryValidityDecision.Keep,
			pending(MemoryCategory.TERM, MemoryAnchor(MemoryAnchorKind.TERM, "2026-1", null), LocalDate(2026, 10, 1))
		)
	}

	@Test
	fun memoriesThatAreNotActiveOrPendingAreNeverJudged() {
		listOf(MemoryState.ARCHIVED, MemoryState.REPLACED, MemoryState.DELETED).forEach { state ->
			val input = MemoryValidityInput(MemoryCategory.PREFERENCE, noAnchor, state, LocalDate(2024, 1, 1), null)

			assertEquals(MemoryValidityDecision.Keep, judge(input))
		}
	}

	@Test
	fun aPreferenceUnusedForMoreThanAYearIsArchived() {
		assertEquals(
			MemoryValidityDecision.Archive(MemoryValidityReason.UNUSED),
			active(MemoryCategory.PREFERENCE, lastUsedOn = LocalDate(2025, 10, 15))
		)
		// Exactly 365 days is still within the year.
		assertEquals(
			MemoryValidityDecision.Keep,
			active(MemoryCategory.PREFERENCE, lastUsedOn = LocalDate(2025, 10, 16))
		)
		assertEquals(MemoryValidityDecision.Keep, active(MemoryCategory.PREFERENCE, lastUsedOn = null))
	}

	@Test
	fun anInterestOfAnotherCareerNeedsConfirmation() {
		assertEquals(
			MemoryValidityDecision.RequestConfirmation(MemoryValidityReason.CAREER_CHANGED),
			active(MemoryCategory.INTEREST, MemoryAnchor(MemoryAnchorKind.CAREER, "OTHER", null))
		)
		assertEquals(
			MemoryValidityDecision.Keep,
			active(MemoryCategory.INTEREST, MemoryAnchor(MemoryAnchorKind.CAREER, "ICI", null))
		)
	}

	@Test
	fun aCareerGoalIsArchivedOnceItIsAchievedOrOutOfReach() {
		val goal = MemoryAnchor(MemoryAnchorKind.CAREER, "ICI", null)

		assertEquals(
			MemoryValidityDecision.Archive(MemoryValidityReason.GOAL_ACHIEVED),
			active(MemoryCategory.CAREER_GOAL, goal, goalOutcome = MemoryGoalOutcome.ACHIEVED)
		)
		assertEquals(
			MemoryValidityDecision.Archive(MemoryValidityReason.GOAL_IMPOSSIBLE),
			active(MemoryCategory.CAREER_GOAL, goal, goalOutcome = MemoryGoalOutcome.IMPOSSIBLE)
		)
	}

	@Test
	fun aCareerGoalOfAnotherCareerNeedsConfirmation() {
		assertEquals(
			MemoryValidityDecision.RequestConfirmation(MemoryValidityReason.CAREER_CHANGED),
			active(MemoryCategory.CAREER_GOAL, MemoryAnchor(MemoryAnchorKind.CAREER, "OTHER", null))
		)
	}

	@Test
	fun aCareerGoalWhoseDatePassedNeedsConfirmation() {
		assertEquals(
			MemoryValidityDecision.RequestConfirmation(MemoryValidityReason.GOAL_DATE_PASSED),
			active(MemoryCategory.CAREER_GOAL, MemoryAnchor(MemoryAnchorKind.CAREER, "ICI", LocalDate(2026, 10, 15)))
		)
		// A goal dated today has not passed yet, and an open goal has no date.
		assertEquals(
			MemoryValidityDecision.Keep,
			active(MemoryCategory.CAREER_GOAL, MemoryAnchor(MemoryAnchorKind.CAREER, "ICI", today))
		)
		assertEquals(
			MemoryValidityDecision.Keep,
			active(MemoryCategory.CAREER_GOAL, MemoryAnchor(MemoryAnchorKind.CAREER, "ICI", null))
		)
	}

	@Test
	fun aTermMemoryNeedsConfirmationOnceItsTermIsNoLongerActive() {
		assertEquals(
			MemoryValidityDecision.RequestConfirmation(MemoryValidityReason.TERM_CLOSED),
			active(MemoryCategory.TERM, MemoryAnchor(MemoryAnchorKind.TERM, "2026-1", null))
		)
		assertEquals(
			MemoryValidityDecision.Keep,
			active(MemoryCategory.TERM, MemoryAnchor(MemoryAnchorKind.TERM, "2026-2", null))
		)
	}

	@Test
	fun aSubjectMemoryIsArchivedWhenItsAttemptIsResolvedOrItsTermClosed() {
		assertEquals(
			MemoryValidityDecision.Archive(MemoryValidityReason.ATTEMPT_RESOLVED),
			active(MemoryCategory.SUBJECT, MemoryAnchor(MemoryAnchorKind.ATTEMPT, "attempt-passed", null))
		)
		assertEquals(
			MemoryValidityDecision.Archive(MemoryValidityReason.TERM_CLOSED),
			active(MemoryCategory.SUBJECT, MemoryAnchor(MemoryAnchorKind.ATTEMPT, "attempt-closed", null))
		)
		assertEquals(
			MemoryValidityDecision.Keep,
			active(MemoryCategory.SUBJECT, MemoryAnchor(MemoryAnchorKind.ATTEMPT, "attempt-open", null))
		)
		assertEquals(
			MemoryValidityDecision.Keep,
			active(MemoryCategory.SUBJECT, MemoryAnchor(MemoryAnchorKind.ATTEMPT, "attempt-unknown", null))
		)
	}

	@Test
	fun aSubjectMemoryIsArchivedWhenItsEvaluationIsGraded() {
		assertEquals(
			MemoryValidityDecision.Archive(MemoryValidityReason.EVALUATION_RESOLVED),
			active(MemoryCategory.SUBJECT, MemoryAnchor(MemoryAnchorKind.EVALUATION, "evaluation-graded", null))
		)
		assertEquals(
			MemoryValidityDecision.Keep,
			active(MemoryCategory.SUBJECT, MemoryAnchor(MemoryAnchorKind.EVALUATION, "evaluation-pending", null))
		)
	}

	@Test
	fun aDatedRestrictionIsArchivedOnceItsDateIsBehind() {
		assertEquals(
			MemoryValidityDecision.Archive(MemoryValidityReason.DATE_PASSED),
			active(MemoryCategory.DATED_RESTRICTION, MemoryAnchor(MemoryAnchorKind.DATE, null, LocalDate(2026, 10, 15)))
		)
		assertEquals(
			MemoryValidityDecision.Keep,
			active(MemoryCategory.DATED_RESTRICTION, MemoryAnchor(MemoryAnchorKind.DATE, null, today))
		)
	}

	@Test
	fun aCategoryWithoutARuleForItsAnchorIsKept() {
		assertEquals(
			MemoryValidityDecision.Keep,
			active(MemoryCategory.INTEREST, MemoryAnchor(MemoryAnchorKind.NONE, "OTHER", null))
		)
		assertEquals(
			MemoryValidityDecision.Keep,
			active(
				MemoryCategory.PREFERENCE,
				MemoryAnchor(MemoryAnchorKind.CAREER, "ICI", null),
				lastUsedOn = LocalDate(2020, 1, 1)
			)
		)
		assertEquals(
			MemoryValidityDecision.Keep,
			active(MemoryCategory.DATED_RESTRICTION, MemoryAnchor(MemoryAnchorKind.NONE, null, LocalDate(2020, 1, 1)))
		)
	}

	@Test
	fun inputsAndDecisionsSurviveAJsonRoundTrip() {
		val input = MemoryValidityInput(
			category = MemoryCategory.CAREER_GOAL,
			anchor = MemoryAnchor(MemoryAnchorKind.CAREER, "ICI", LocalDate(2026, 12, 1)),
			state = MemoryState.ACTIVE,
			lastUsedOn = LocalDate(2026, 10, 1),
			pendingSince = null,
			goalOutcome = MemoryGoalOutcome.OPEN
		)
		// Typed as the sealed interface, so the encoding carries the class discriminator the backend reads back.
		val decision: MemoryValidityDecision =
			MemoryValidityDecision.RequestConfirmation(MemoryValidityReason.GOAL_DATE_PASSED)

		assertEquals(input, Json.decodeFromString<MemoryValidityInput>(Json.encodeToString(input)))
		assertEquals(decision, Json.decodeFromString<MemoryValidityDecision>(Json.encodeToString(decision)))
	}
}
