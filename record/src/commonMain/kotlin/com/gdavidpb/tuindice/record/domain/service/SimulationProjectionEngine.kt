package com.gdavidpb.tuindice.record.domain.service

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.base.utils.extension.round
import com.gdavidpb.tuindice.record.data.model.quarter.LocalQuarter
import com.gdavidpb.tuindice.record.data.model.quarter.LocalSubject

class SimulationProjectionEngine {
	private data class SubjectAttempt(
		val subjectId: String,
		val grade: Int
	)

	private data class EffectiveAttempt(
		val grade: Int,
		val credits: Int
	) {
		val weighted: Long = grade.toLong() * credits.toLong()
	}

	private class CodeState {
		private var latest: SubjectAttempt? = null
		private var second: SubjectAttempt? = null

		fun add(attempt: SubjectAttempt) {
			second = latest
			latest = attempt
		}

		fun previousSubjectIdWithoutEffect(): String? {
			val currentLatest = latest
			val currentSecond = second

			return if (
				(currentLatest != null) &&
				(currentSecond != null) &&
				(currentLatest.grade >= MIN_APPROVED_GRADE)
			) {
				currentSecond.subjectId
			} else {
				null
			}
		}
	}

	private class EffectiveCodeState {
		private var latest: EffectiveAttempt? = null
		private var second: EffectiveAttempt? = null

		private var weightedSum = 0L
		private var creditsSum = 0L

		val effectiveWeighted: Long
			get() {
				val currentLatest = latest
				val currentSecond = second

				return when {
					currentLatest == null -> 0L
					currentSecond == null -> weightedSum
					currentLatest.grade >= MIN_APPROVED_GRADE -> weightedSum - currentSecond.weighted
					else -> weightedSum
				}
			}

		val effectiveCredits: Long
			get() {
				val currentLatest = latest
				val currentSecond = second

				return when {
					currentLatest == null -> 0L
					currentSecond == null -> creditsSum
					currentLatest.grade >= MIN_APPROVED_GRADE -> creditsSum - currentSecond.credits.toLong()
					else -> creditsSum
				}
			}

		fun add(attempt: EffectiveAttempt) {
			second = latest
			latest = attempt
			weightedSum += attempt.weighted
			creditsSum += attempt.credits.toLong()
		}
	}

	fun recompute(quarters: List<LocalQuarter>): List<LocalQuarter> {
		if (quarters.isEmpty()) return emptyList()

		val quartersAscending = quarters.sortedWith(
			compareBy<LocalQuarter> { quarter -> quarter.startDate }
				.thenByDescending { quarter -> quarter.id }
		)
		val excludedSubjectIds = resolveExcludedSubjectIds(quartersAscending)
		val codeStates = hashMapOf<String, EffectiveCodeState>()

		var cumulativeWeighted = 0L
		var cumulativeCredits = 0L

		val recomputedAscending = quartersAscending.map { quarter ->
			var quarterWeighted = 0L
			var quarterCredits = 0L

			val recomputedSubjects = quarter.subjects.map { subject ->
				val simulationStatus = when {
					(subject.status != null) && (subject.status != SubjectStatus.NORMAL) -> null
					subject.id in excludedSubjectIds -> SubjectStatus.WITHOUT_EFFECT
					else -> null
				}

				if (subject.grade > 0) {
					val weighted = subject.grade.toLong() * subject.credits.toLong()
					quarterWeighted += weighted
					quarterCredits += subject.credits.toLong()
				}

				subject.copy(simulationStatus = simulationStatus)
			}

			val sortedSubjects = if (quarter.subjects.size > 1) {
				quarter.subjects.sortedByDescending(LocalSubject::id)
			} else {
				quarter.subjects
			}

			sortedSubjects.forEach { subject ->
				if (subject.grade <= 0) return@forEach

				val state = codeStates.getOrPut(subject.code, ::EffectiveCodeState)
				val previousWeighted = state.effectiveWeighted
				val previousCredits = state.effectiveCredits

				state.add(
					EffectiveAttempt(
						grade = subject.grade,
						credits = subject.credits
					)
				)

				cumulativeWeighted += state.effectiveWeighted - previousWeighted
				cumulativeCredits += state.effectiveCredits - previousCredits
			}

			val simulatedQuarter = if (quarter.isReadOnly) {
				quarter.copy(
					simulationGrade = quarter.grade,
					simulationGradeSum = quarter.gradeSum,
					simulationCredits = quarter.credits,
					simulationCreditsSum = quarter.creditsSum,
					subjects = recomputedSubjects
				)
			} else {
				quarter.copy(
					simulationGrade = computeAverage(
						weighted = quarterWeighted,
						credits = quarterCredits
					),
					simulationGradeSum = computeAverage(
						weighted = cumulativeWeighted,
						credits = cumulativeCredits
					),
					simulationCredits = quarterCredits.toInt(),
					simulationCreditsSum = cumulativeCredits.toInt(),
					subjects = recomputedSubjects
				)
			}

			simulatedQuarter
		}

		return recomputedAscending.asReversed()
	}

	private fun resolveExcludedSubjectIds(
		quartersAscending: List<LocalQuarter>
	): Set<String> {
		val codeStates = hashMapOf<String, CodeState>()

		quartersAscending.forEach { quarter ->
			val sortedSubjects = if (quarter.subjects.size > 1) {
				quarter.subjects.sortedByDescending(LocalSubject::id)
			} else {
				quarter.subjects
			}

			sortedSubjects.forEach { subject ->
				if (subject.grade <= 0) return@forEach

				codeStates
					.getOrPut(subject.code, ::CodeState)
					.add(
						SubjectAttempt(
							subjectId = subject.id,
							grade = subject.grade
						)
					)
			}
		}

		return buildSet {
			codeStates.values.forEach { state ->
				state.previousSubjectIdWithoutEffect()?.let(::add)
			}
		}
	}

	private fun computeAverage(weighted: Long, credits: Long): Double {
		if (credits == 0L) return 0.0

		return (weighted.toDouble() / credits.toDouble())
			.round(4)
	}

	private companion object {
		private const val MIN_APPROVED_GRADE = 3
	}
}
