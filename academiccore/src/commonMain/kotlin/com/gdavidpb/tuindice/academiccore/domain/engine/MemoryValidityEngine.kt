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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * Judges whether a memory still holds, told from its facts and the academic state it is checked against. It is a pure
 * function: it reads and writes nothing, and the caller applies the decision.
 *
 * A memory waiting for confirmation is only archived after [UNCONFIRMED_DAYS] without an answer, whatever its category;
 * the category rules judge the active memories. Archived, replaced and deleted memories are never judged, and a
 * category paired with an anchor that has no rule is kept.
 */
object MemoryValidityEngine {
	private const val UNCONFIRMED_DAYS = 30
	private const val UNUSED_DAYS = 365

	fun decide(input: MemoryValidityInput, snapshot: MemoryValiditySnapshot): MemoryValidityDecision {
		return when (input.state) {
			MemoryState.PENDING_CONFIRMATION -> unconfirmedDecision(input.pendingSince, snapshot.today)
			MemoryState.ACTIVE -> activeDecision(input, snapshot)
			MemoryState.ARCHIVED, MemoryState.REPLACED, MemoryState.DELETED -> MemoryValidityDecision.Keep
		}
	}

	private fun unconfirmedDecision(pendingSince: LocalDate?, today: LocalDate): MemoryValidityDecision =
		if (pendingSince != null && pendingSince.plus(UNCONFIRMED_DAYS, DateTimeUnit.DAY) < today) {
			MemoryValidityDecision.Archive(MemoryValidityReason.UNCONFIRMED)
		} else {
			MemoryValidityDecision.Keep
		}

	private fun activeDecision(input: MemoryValidityInput, snapshot: MemoryValiditySnapshot): MemoryValidityDecision {
		val anchor = input.anchor

		return when (input.category to anchor.kind) {
			MemoryCategory.PREFERENCE to MemoryAnchorKind.NONE ->
				unusedDecision(input.lastUsedOn, snapshot.today)
			MemoryCategory.INTEREST to MemoryAnchorKind.CAREER ->
				careerDecision(anchor, snapshot.careerCode)
			MemoryCategory.CAREER_GOAL to MemoryAnchorKind.CAREER ->
				careerGoalDecision(input.goalOutcome, anchor, snapshot)
			MemoryCategory.TERM to MemoryAnchorKind.TERM ->
				termDecision(anchor.ref, snapshot.activeTermKeys)
			MemoryCategory.SUBJECT to MemoryAnchorKind.ATTEMPT ->
				attemptDecision(anchor.ref, snapshot)
			MemoryCategory.SUBJECT to MemoryAnchorKind.EVALUATION ->
				evaluationDecision(anchor.ref, snapshot.resolvedEvaluationIds)
			MemoryCategory.DATED_RESTRICTION to MemoryAnchorKind.DATE ->
				dateDecision(anchor.date, snapshot.today)
			else -> MemoryValidityDecision.Keep
		}
	}

	private fun unusedDecision(lastUsedOn: LocalDate?, today: LocalDate): MemoryValidityDecision =
		if (lastUsedOn != null && lastUsedOn.plus(UNUSED_DAYS, DateTimeUnit.DAY) < today) {
			MemoryValidityDecision.Archive(MemoryValidityReason.UNUSED)
		} else {
			MemoryValidityDecision.Keep
		}

	private fun careerDecision(anchor: MemoryAnchor, careerCode: String?): MemoryValidityDecision =
		if (anchor.ref != careerCode) {
			MemoryValidityDecision.RequestConfirmation(MemoryValidityReason.CAREER_CHANGED)
		} else {
			MemoryValidityDecision.Keep
		}

	private fun careerGoalDecision(
		goalOutcome: MemoryGoalOutcome,
		anchor: MemoryAnchor,
		snapshot: MemoryValiditySnapshot
	): MemoryValidityDecision {
		val goalDate = anchor.date

		return when (goalOutcome) {
			MemoryGoalOutcome.ACHIEVED -> MemoryValidityDecision.Archive(MemoryValidityReason.GOAL_ACHIEVED)
			MemoryGoalOutcome.IMPOSSIBLE -> MemoryValidityDecision.Archive(MemoryValidityReason.GOAL_IMPOSSIBLE)
			MemoryGoalOutcome.OPEN -> when {
				anchor.ref != snapshot.careerCode ->
					MemoryValidityDecision.RequestConfirmation(MemoryValidityReason.CAREER_CHANGED)
				goalDate != null && goalDate < snapshot.today ->
					MemoryValidityDecision.RequestConfirmation(MemoryValidityReason.GOAL_DATE_PASSED)
				else -> MemoryValidityDecision.Keep
			}
		}
	}

	private fun termDecision(termKey: String?, activeTermKeys: Set<String>): MemoryValidityDecision =
		if (termKey != null && termKey in activeTermKeys) {
			MemoryValidityDecision.Keep
		} else {
			MemoryValidityDecision.RequestConfirmation(MemoryValidityReason.TERM_CLOSED)
		}

	private fun attemptDecision(attemptId: String?, snapshot: MemoryValiditySnapshot): MemoryValidityDecision {
		val termKey = attemptId?.let { snapshot.attemptTermKeys[it] }

		return when {
			attemptId != null && attemptId in snapshot.resolvedAttemptIds ->
				MemoryValidityDecision.Archive(MemoryValidityReason.ATTEMPT_RESOLVED)
			termKey != null && termKey !in snapshot.activeTermKeys ->
				MemoryValidityDecision.Archive(MemoryValidityReason.TERM_CLOSED)
			else -> MemoryValidityDecision.Keep
		}
	}

	private fun evaluationDecision(evaluationId: String?, resolvedEvaluationIds: Set<String>): MemoryValidityDecision =
		if (evaluationId != null && evaluationId in resolvedEvaluationIds) {
			MemoryValidityDecision.Archive(MemoryValidityReason.EVALUATION_RESOLVED)
		} else {
			MemoryValidityDecision.Keep
		}

	private fun dateDecision(date: LocalDate?, today: LocalDate): MemoryValidityDecision =
		if (date != null && date < today) {
			MemoryValidityDecision.Archive(MemoryValidityReason.DATE_PASSED)
		} else {
			MemoryValidityDecision.Keep
		}
}
