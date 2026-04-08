package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.*
import kotlin.math.abs

private const val DEFAULT_SIMULATION_NUMERIC_GRADE = 5
private const val MIN_APPROVED_GRADE = 3

object AcademicProjectionEngine {
	fun reproject(record: AcademicRecord): AcademicRecord {
		val officialProjection = project(
			viewMode = ProjectionViewMode.OFFICIAL,
			terms = record.officialSnapshot.terms,
			overlay = AcademicOverlay()
		)
		val simulationProjection = project(
			viewMode = ProjectionViewMode.SIMULATION,
			terms = record.officialSnapshot.terms + record.localOverlay.syntheticTerms,
			overlay = record.localOverlay,
			officialProjectionByTermId = officialProjection.terms.associateBy(TermProjection::id)
		)

		return record.copy(
			officialProjection = officialProjection,
			simulationProjection = simulationProjection,
			summary = RecordSummary(
				official = AcademicSummaryEngine.summarize(officialProjection),
				simulation = AcademicSummaryEngine.summarize(simulationProjection)
			)
		)
	}

	fun project(
		viewMode: ProjectionViewMode,
		terms: List<AcademicTerm>,
		overlay: AcademicOverlay,
		officialProjectionByTermId: Map<String, TermProjection> = emptyMap()
	): RecordProjection {
		if (terms.isEmpty()) {
			return RecordProjection(
				viewMode = viewMode,
				terms = emptyList()
			)
		}

		val overridesByAttemptId = overlay.attemptOverrides.associateBy(AttemptOverride::attemptId)
		val termsAscending = terms.sortedWith(
			compareBy<AcademicTerm> { it.startAtMillis }
				.thenByDescending { it.id }
		)
		val effectiveTermsAscending = termsAscending.map { term ->
			EffectiveTermState(
				term = term,
				attempts = term.attempts
					.sortedWith(
						compareBy<AcademicAttempt> { it.sequenceInTerm }
							.thenByDescending { it.id }
					)
					.map { attempt ->
						resolveEffectiveAttempt(
							attempt = attempt,
							override = overridesByAttemptId[attempt.id],
							viewMode = viewMode
						)
					}
			)
		}
		val excludedAttemptIds = resolveExcludedAttemptIds(effectiveTermsAscending)
		val projectionsAscending = mutableListOf<TermProjection>()
		val codeStates = hashMapOf<String, EffectiveCodeState>()
		var cumulativeWeighted = 0L
		var cumulativeCredits = 0L

		effectiveTermsAscending.forEach { termState ->
			val termWeighted = termState.attempts.sumOf { attempt ->
				if (attempt.countsTowardTermAverage) attempt.numericWeightedContribution() else 0L
			}
			val termCredits = termState.attempts.sumOf { attempt ->
				if (attempt.countsTowardTermAverage) attempt.attempt.credits else 0
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

			val officialTermProjection = officialProjectionByTermId[termState.term.id]
			val useFrozenOfficialMetrics =
				viewMode == ProjectionViewMode.SIMULATION &&
					termState.term.kind.isOfficialHistorical &&
					officialTermProjection != null

			val attemptProjections = termState.attempts.map { attempt ->
				val badge = when {
					attempt.badge == HistoricalBadge.WITHOUT_EFFECT -> HistoricalBadge.WITHOUT_EFFECT
					viewMode == ProjectionViewMode.SIMULATION && attempt.attempt.id in excludedAttemptIds ->
						HistoricalBadge.WITHOUT_EFFECT

					else -> HistoricalBadge.NONE
				}

				AttemptProjection(
					id = attempt.attempt.id,
					termId = attempt.attempt.termId,
					subjectCode = attempt.attempt.subjectCode,
					subjectName = attempt.attempt.subjectName,
					credits = attempt.attempt.credits,
					sequenceInTerm = attempt.attempt.sequenceInTerm,
					gradingMode = attempt.attempt.gradingMode,
					rawGradeToken = attempt.attempt.rawGradeToken,
					rawObservationText = attempt.attempt.rawObservationText,
					score = attempt.score,
					outcome = attempt.outcome,
					badge = badge,
					editable = attempt.attempt.editable,
					synthetic = attempt.attempt.synthetic,
					countsTowardTermAverage = attempt.countsTowardTermAverage,
					countsTowardCumulativeAverage = attempt.countsTowardCumulativeAverage &&
						attempt.attempt.id !in excludedAttemptIds
				)
			}

			projectionsAscending += if (useFrozenOfficialMetrics) {
				TermProjection(
					id = termState.term.id,
					label = termState.term.label,
					startAtMillis = termState.term.startAtMillis,
					endAtMillis = termState.term.endAtMillis,
					order = termState.term.order,
					kind = termState.term.kind,
					grade = officialTermProjection.grade,
					gradeSum = officialTermProjection.gradeSum,
					credits = officialTermProjection.credits,
					creditsSum = officialTermProjection.creditsSum,
					attempts = attemptProjections
				)
			} else {
				TermProjection(
					id = termState.term.id,
					label = termState.term.label,
					startAtMillis = termState.term.startAtMillis,
					endAtMillis = termState.term.endAtMillis,
					order = termState.term.order,
					kind = termState.term.kind,
					grade = computeAverage(termWeighted, termCredits.toLong()),
					gradeSum = computeAverage(cumulativeWeighted, cumulativeCredits),
					credits = termCredits,
					creditsSum = cumulativeCredits.toInt(),
					attempts = attemptProjections
				)
			}
		}

		return RecordProjection(
			viewMode = viewMode,
			terms = projectionsAscending.asReversed()
		)
	}

	private fun resolveEffectiveAttempt(
		attempt: AcademicAttempt,
		override: AttemptOverride?,
		viewMode: ProjectionViewMode
	): EffectiveAttemptState {
		val score = when {
			viewMode == ProjectionViewMode.SIMULATION && override?.score != null -> override.score
			viewMode == ProjectionViewMode.SIMULATION &&
				attempt.editable &&
				attempt.gradingMode == AttemptGradingMode.NUMERIC &&
				attempt.officialOutcome == OfficialOutcome.PENDING &&
				attempt.officialScore.kind == AttemptScoreKind.EMPTY ->
				AttemptScore.numeric(DEFAULT_SIMULATION_NUMERIC_GRADE)

			else -> attempt.officialScore
		}
		val preferredOutcome = when {
			viewMode == ProjectionViewMode.SIMULATION && override?.outcome != null -> override.outcome
			else -> attempt.officialOutcome
		}
		val outcome = resolveOutcome(attempt.gradingMode, score, preferredOutcome)

		return EffectiveAttemptState(
			attempt = attempt,
			score = score,
			outcome = outcome,
			badge = attempt.officialBadge,
			countsTowardTermAverage = countsTowardTermAverage(attempt.gradingMode, score, outcome),
			countsTowardCumulativeAverage = countsTowardCumulativeAverage(attempt.gradingMode, score, outcome),
			approvalEvent = outcome == OfficialOutcome.APPROVED,
			countsTowardRetakeTimeline = countsTowardRetakeTimeline(attempt.gradingMode, score, outcome)
		)
	}

	private fun resolveOutcome(
		gradingMode: AttemptGradingMode,
		score: AttemptScore,
		preferredOutcome: OfficialOutcome
	): OfficialOutcome {
		if (preferredOutcome != OfficialOutcome.PENDING) return preferredOutcome

		return when (gradingMode) {
			AttemptGradingMode.NUMERIC -> when (score.numericValue ?: 0) {
				in Int.MIN_VALUE until 1 -> OfficialOutcome.PENDING
				in 1 until MIN_APPROVED_GRADE -> OfficialOutcome.FAILED
				else -> OfficialOutcome.APPROVED
			}

			AttemptGradingMode.QUALITATIVE_PASS_FAIL -> when (score.symbolicValue?.trim()?.uppercase()) {
				"A" -> OfficialOutcome.APPROVED
				"R" -> OfficialOutcome.RETIRED
				else -> OfficialOutcome.PENDING
			}
		}
	}

	private fun countsTowardTermAverage(
		gradingMode: AttemptGradingMode,
		score: AttemptScore,
		outcome: OfficialOutcome
	): Boolean {
		val numericValue = score.numericValue ?: 0

		return gradingMode == AttemptGradingMode.NUMERIC &&
			outcome !in setOf(OfficialOutcome.PENDING, OfficialOutcome.RETIRED) &&
			(numericValue > 0 || outcome == OfficialOutcome.UNREPORTED)
	}

	private fun countsTowardCumulativeAverage(
		gradingMode: AttemptGradingMode,
		score: AttemptScore,
		outcome: OfficialOutcome
	): Boolean {
		val numericValue = score.numericValue ?: 0

		return gradingMode == AttemptGradingMode.NUMERIC &&
			outcome !in setOf(
				OfficialOutcome.PENDING,
				OfficialOutcome.RETIRED,
				OfficialOutcome.UNREPORTED
			) &&
			numericValue > 0
	}

	private fun countsTowardRetakeTimeline(
		gradingMode: AttemptGradingMode,
		score: AttemptScore,
		outcome: OfficialOutcome
	): Boolean {
		return countsTowardCumulativeAverage(gradingMode, score, outcome) || (
			gradingMode == AttemptGradingMode.QUALITATIVE_PASS_FAIL &&
				outcome in setOf(OfficialOutcome.APPROVED, OfficialOutcome.FAILED)
			)
	}

	private fun resolveExcludedAttemptIds(termsAscending: List<EffectiveTermState>): Set<String> {
		val states = hashMapOf<String, ExclusionCodeState>()

		termsAscending.forEach { term ->
			term.attempts.forEach { attempt ->
				if (!attempt.countsTowardRetakeTimeline) return@forEach

				states.getOrPut(attempt.attempt.subjectCode, ::ExclusionCodeState).add(
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

	private fun EffectiveAttemptState.numericWeightedContribution(): Long {
		val numericValue = score.numericValue ?: 0
		return numericValue.toLong() * attempt.credits.toLong()
	}

	private fun computeAverage(weighted: Long, credits: Long): Double {
		return truncateScaledDivision(weighted, credits, decimals = 4)
	}

	private fun truncateScaledDivision(numerator: Long, denominator: Long, decimals: Int): Double {
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
		repeat(decimals) { scale *= 10L }
		return scale
	}

	private data class EffectiveTermState(
		val term: AcademicTerm,
		val attempts: List<EffectiveAttemptState>
	)

	private data class EffectiveAttemptState(
		val attempt: AcademicAttempt,
		val score: AttemptScore,
		val outcome: OfficialOutcome,
		val badge: HistoricalBadge,
		val countsTowardTermAverage: Boolean,
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
