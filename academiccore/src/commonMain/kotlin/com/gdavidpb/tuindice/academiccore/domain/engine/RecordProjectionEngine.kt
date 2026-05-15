package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.*
import kotlin.math.abs

private const val DEFAULT_PROJECTION_NUMERIC_GRADE = 5
private const val MIN_APPROVED_GRADE = 3

object RecordProjectionEngine {
	fun projectAcademic(record: AcademicRecord): RecordProjection {
		return project(
			terms = record.terms.filterNot { term -> term.kind.isSynthetic },
			attemptOverrides = emptyMap(),
			academicProjectionByTermId = emptyMap(),
			includeProjectionBehavior = false
		)
	}

	fun projectProjection(record: AcademicRecord): RecordProjection {
		val academicProjection = projectAcademic(record)

		return project(
			terms = record.terms,
			attemptOverrides = record.attemptOverrides.associateBy(AttemptOverride::attemptId),
			academicProjectionByTermId = academicProjection.terms.associateBy(TermProjection::id),
			includeProjectionBehavior = true
		)
	}

	private fun project(
		terms: List<AcademicTerm>,
		attemptOverrides: Map<String, AttemptOverride>,
		academicProjectionByTermId: Map<String, TermProjection>,
		includeProjectionBehavior: Boolean
	): RecordProjection {
		if (terms.isEmpty()) return RecordProjection()

		val termsAscending = terms.sortedWith(compareBy(AcademicTerm::termOrder, AcademicTerm::id))
		val effectiveTermsAscending = termsAscending.map { term ->
			EffectiveTermState(
				term = term,
				attempts = term.attempts.map { attempt ->
					resolveEffectiveAttempt(
						attempt = attempt,
						termKind = term.kind,
						attemptOverride = attemptOverrides[attempt.id],
						includeProjectionBehavior = includeProjectionBehavior
					)
				}
			)
		}
		val visibleTermsAscending = filterObsoleteSyntheticAttempts(effectiveTermsAscending)
		val excludedAttemptIds = resolveExcludedAttemptIds(visibleTermsAscending)
		val projectionsAscending = mutableListOf<TermProjection>()
		val codeStates = hashMapOf<String, EffectiveCodeState>()
		var cumulativeWeighted = 0L
		var cumulativeCredits = 0L

		visibleTermsAscending.forEach { termState ->
			val periodAverageCredits = termState.attempts.sumOf { attempt ->
				if (attempt.countsTowardPeriodAverage) attempt.attempt.credits else 0
			}
			val periodDisplayedCredits = termState.attempts.sumOf { attempt ->
				if (attempt.countsTowardDisplayedPeriodCredits) attempt.attempt.credits else 0
			}
			val periodWeighted = termState.attempts.sumOf { attempt ->
				if (attempt.countsTowardPeriodAverage) attempt.numericWeightedContribution() else 0L
			}

			termState.attempts.forEach { attempt ->
				if (!attempt.countsTowardRetakeTimeline) return@forEach

				val state = codeStates.getOrPut(attempt.attempt.subjectCode, ::EffectiveCodeState)
				val previousWeighted = state.effectiveWeighted
				val previousCredits = state.effectiveCredits

				state.add(
					EffectiveAttempt(
						approvalEvent = attempt.approvalEvent,
						credits = if (attempt.countsTowardCumulativeAverage) attempt.attempt.credits else 0,
						weighted = if (attempt.countsTowardCumulativeAverage) attempt.numericWeightedContribution() else 0L
					)
				)

				cumulativeWeighted += state.effectiveWeighted - previousWeighted
				cumulativeCredits += state.effectiveCredits - previousCredits
			}

			val academicTermProjection = academicProjectionByTermId[termState.term.id]
			val useFrozenAcademicMetrics = includeProjectionBehavior &&
				termState.term.kind.isHistorical &&
				academicTermProjection != null

			val attemptProjections = termState.attempts.map { attempt ->
				val badge = when {
					attempt.badge == AttemptBadge.WITHOUT_EFFECT -> AttemptBadge.WITHOUT_EFFECT
					includeProjectionBehavior && attempt.attempt.id in excludedAttemptIds -> AttemptBadge.WITHOUT_EFFECT
					else -> AttemptBadge.NONE
				}

				AttemptProjection(
					id = attempt.attempt.id,
					subjectCode = attempt.attempt.subjectCode,
					subjectName = attempt.attempt.subjectName,
					credits = attempt.attempt.credits,
					gradingMode = attempt.attempt.gradingMode,
					score = attempt.score,
					outcome = attempt.outcome,
					badge = badge
				)
			}

			projectionsAscending += if (useFrozenAcademicMetrics) {
				TermProjection(
					id = termState.term.id,
					periodYear = termState.term.periodYear,
					periodCode = termState.term.periodCode,
					termKey = termState.term.termKey,
					termOrder = termState.term.termOrder,
					periodLabel = termState.term.periodLabel,
					kind = termState.term.kind,
					periodAverage = academicTermProjection.periodAverage,
					cumulativeAverage = academicTermProjection.cumulativeAverage,
					periodCredits = academicTermProjection.periodCredits,
					cumulativeCredits = academicTermProjection.cumulativeCredits,
					attempts = attemptProjections
				)
			} else {
				TermProjection(
					id = termState.term.id,
					periodYear = termState.term.periodYear,
					periodCode = termState.term.periodCode,
					termKey = termState.term.termKey,
					termOrder = termState.term.termOrder,
					periodLabel = termState.term.periodLabel,
					kind = termState.term.kind,
					periodAverage = computeAverage(
						weighted = periodWeighted,
						credits = periodAverageCredits.toLong()
					),
					cumulativeAverage = computeAverage(
						weighted = cumulativeWeighted,
						credits = cumulativeCredits
					),
					periodCredits = periodDisplayedCredits,
					cumulativeCredits = cumulativeCredits.toInt(),
					attempts = attemptProjections
				)
			}
		}

		return RecordProjection(
			terms = projectionsAscending.asReversed()
		)
	}

	private fun resolveEffectiveAttempt(
		attempt: AcademicAttempt,
		termKind: TermKind,
		attemptOverride: AttemptOverride?,
		includeProjectionBehavior: Boolean
	): EffectiveAttemptState {
		val score = when {
			includeProjectionBehavior && attemptOverride?.score != null -> attemptOverride.score
			includeProjectionBehavior &&
				termKind.isEditable &&
				attempt.gradingMode == AttemptGradingMode.NUMERIC &&
				attempt.academicOutcome == AttemptOutcome.PENDING &&
				attempt.academicScore is AttemptScore.Empty ->
				AttemptScore.numeric(DEFAULT_PROJECTION_NUMERIC_GRADE)

			else -> attempt.academicScore
		}
		val baseOutcome = when {
			includeProjectionBehavior && attemptOverride?.outcome != null -> attemptOverride.outcome
			else -> attempt.academicOutcome
		}
		val resolvedOutcome = resolveOutcome(
			gradingMode = attempt.gradingMode,
			score = score,
			preferredOutcome = baseOutcome
		)

		return EffectiveAttemptState(
			attempt = attempt,
			score = score,
			outcome = resolvedOutcome,
			badge = attempt.academicBadge,
			countsTowardPeriodAverage = countsTowardPeriodAverage(
				gradingMode = attempt.gradingMode,
				score = score,
				outcome = resolvedOutcome
			),
			countsTowardDisplayedPeriodCredits = countsTowardDisplayedPeriodCredits(
				gradingMode = attempt.gradingMode,
				outcome = resolvedOutcome
			),
			countsTowardCumulativeAverage = countsTowardCumulativeAverage(
				gradingMode = attempt.gradingMode,
				score = score,
				outcome = resolvedOutcome
			),
			approvalEvent = resolvedOutcome == AttemptOutcome.APPROVED,
			countsTowardRetakeTimeline = countsTowardRetakeTimeline(
				gradingMode = attempt.gradingMode,
				score = score,
				outcome = resolvedOutcome
			)
		)
	}

	private fun resolveOutcome(
		gradingMode: AttemptGradingMode,
		score: AttemptScore,
		preferredOutcome: AttemptOutcome
	): AttemptOutcome {
		if (preferredOutcome != AttemptOutcome.PENDING) return preferredOutcome

		return when (gradingMode) {
			AttemptGradingMode.NUMERIC -> when (score.numericValue ?: 0) {
				in Int.MIN_VALUE until 1 -> AttemptOutcome.PENDING
				in 1 until MIN_APPROVED_GRADE -> AttemptOutcome.FAILED
				else -> AttemptOutcome.APPROVED
			}

			AttemptGradingMode.QUALITATIVE_PASS_FAIL -> when (score.symbolicValue?.trim()?.uppercase()) {
				"A" -> AttemptOutcome.APPROVED
				"R" -> AttemptOutcome.RETIRED
				else -> AttemptOutcome.PENDING
			}
		}
	}

	private fun countsTowardPeriodAverage(
		gradingMode: AttemptGradingMode,
		score: AttemptScore,
		outcome: AttemptOutcome
	): Boolean {
		val numericValue = score.numericValue ?: 0

		return gradingMode == AttemptGradingMode.NUMERIC &&
			outcome !in setOf(
				AttemptOutcome.PENDING,
				AttemptOutcome.RETIRED
			) &&
			(
				numericValue > 0 ||
					outcome == AttemptOutcome.UNREPORTED
				)
	}

	private fun countsTowardCumulativeAverage(
		gradingMode: AttemptGradingMode,
		score: AttemptScore,
		outcome: AttemptOutcome
	): Boolean {
		val numericValue = score.numericValue ?: 0

		return gradingMode == AttemptGradingMode.NUMERIC &&
			outcome !in setOf(
				AttemptOutcome.PENDING,
				AttemptOutcome.RETIRED,
				AttemptOutcome.UNREPORTED
			) &&
			numericValue > 0
	}

	private fun countsTowardDisplayedPeriodCredits(
		gradingMode: AttemptGradingMode,
		outcome: AttemptOutcome
	): Boolean {
		return when (gradingMode) {
			AttemptGradingMode.NUMERIC -> outcome !in setOf(
				AttemptOutcome.PENDING,
				AttemptOutcome.RETIRED
			)

			AttemptGradingMode.QUALITATIVE_PASS_FAIL -> outcome != AttemptOutcome.RETIRED
		}
	}

	private fun countsTowardRetakeTimeline(
		gradingMode: AttemptGradingMode,
		score: AttemptScore,
		outcome: AttemptOutcome
	): Boolean {
		return countsTowardCumulativeAverage(
			gradingMode = gradingMode,
			score = score,
			outcome = outcome
		) || (
			gradingMode == AttemptGradingMode.QUALITATIVE_PASS_FAIL &&
				outcome in setOf(AttemptOutcome.APPROVED, AttemptOutcome.FAILED)
			)
	}

	private fun resolveExcludedAttemptIds(termsAscending: List<EffectiveTermState>): Set<String> {
		val states = hashMapOf<String, ExclusionCodeState>()

		termsAscending.forEach { term ->
			term.attempts.forEach { attempt ->
				if (!attempt.countsTowardRetakeTimeline) return@forEach

				states
					.getOrPut(attempt.attempt.subjectCode, ::ExclusionCodeState)
					.add(
						SubjectAttempt(
							attemptId = attempt.attempt.id,
							approvalEvent = attempt.approvalEvent
						)
					)
			}
		}

		return buildSet {
			states.values.forEach { state ->
				state.previousAttemptIdWithoutEffect()?.let(::add)
			}
		}
	}

	private fun filterObsoleteSyntheticAttempts(
		termsAscending: List<EffectiveTermState>
	): List<EffectiveTermState> {
		val approvedSubjectCodes = hashSetOf<String>()

		return termsAscending.map { termState ->
			val visibleAttempts = termState.attempts.filter { attempt ->
				!termState.term.kind.isSynthetic || attempt.attempt.subjectCode !in approvedSubjectCodes
			}

			visibleAttempts.forEach { attempt ->
				if (attempt.approvalEvent) {
					approvedSubjectCodes += attempt.attempt.subjectCode
				}
			}

			termState.copy(attempts = visibleAttempts)
		}
	}

	private fun EffectiveAttemptState.numericWeightedContribution(): Long {
		val numericValue = score.numericValue ?: 0
		return numericValue.toLong() * attempt.credits.toLong()
	}

	private fun computeAverage(weighted: Long, credits: Long): Double {
		return truncateScaledDivision(
			numerator = weighted,
			denominator = credits,
			decimals = 4
		)
	}

	private fun truncateScaledDivision(
		numerator: Long,
		denominator: Long,
		decimals: Int
	): Double {
		if (denominator == 0L) return 0.0
		if (decimals <= 0) return (numerator / denominator).toDouble()

		val scale = decimalScale(decimals)
		val negative = (numerator < 0L) xor (denominator < 0L)
		val scaled = (abs(numerator) * scale) / abs(denominator)
		val normalized = scaled.toDouble() / scale.toDouble()

		return if (negative) -normalized else normalized
	}

	private fun decimalScale(decimals: Int): Long {
		var scale = 1L

		repeat(decimals) {
			scale *= 10L
		}

		return scale
	}

	private data class EffectiveTermState(
		val term: AcademicTerm,
		val attempts: List<EffectiveAttemptState>
	)

	private data class EffectiveAttemptState(
		val attempt: AcademicAttempt,
		val score: AttemptScore,
		val outcome: AttemptOutcome,
		val badge: AttemptBadge,
		val countsTowardPeriodAverage: Boolean,
		val countsTowardDisplayedPeriodCredits: Boolean,
		val countsTowardCumulativeAverage: Boolean,
		val approvalEvent: Boolean,
		val countsTowardRetakeTimeline: Boolean
	)

	private data class SubjectAttempt(
		val attemptId: String,
		val approvalEvent: Boolean
	)

	private class ExclusionCodeState {
		private var latest: SubjectAttempt? = null
		private var previous: SubjectAttempt? = null

		fun add(attempt: SubjectAttempt) {
			previous = latest
			latest = attempt
		}

		fun previousAttemptIdWithoutEffect(): String? {
			return if (latest?.approvalEvent == true) previous?.attemptId else null
		}
	}

	private data class EffectiveAttempt(
		val approvalEvent: Boolean,
		val credits: Int,
		val weighted: Long
	)

	private class EffectiveCodeState {
		private var latest: EffectiveAttempt? = null
		private var previous: EffectiveAttempt? = null
		private var weightedSum: Long = 0L
		private var creditsSum: Long = 0L

		val effectiveWeighted: Long
			get() = when {
				latest == null -> 0L
				previous == null -> weightedSum
				latest?.approvalEvent == true -> weightedSum - previous!!.weighted
				else -> weightedSum
			}

		val effectiveCredits: Long
			get() = when {
				latest == null -> 0L
				previous == null -> creditsSum
				latest?.approvalEvent == true -> creditsSum - previous!!.credits.toLong()
				else -> creditsSum
			}

		fun add(attempt: EffectiveAttempt) {
			previous = latest
			latest = attempt
			weightedSum += attempt.weighted
			creditsSum += attempt.credits.toLong()
		}
	}
}
