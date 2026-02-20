package com.gdavidpb.tuindice.record.data.utils

import com.gdavidpb.tuindice.base.utils.extension.round
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject

class IndexComputationEngine {
	data class RecomputeResult(
		val quarters: List<LocalQuarter>,
		val affectedQuarters: List<LocalQuarter>
	)

	private data class CodeAttempt(
		val grade: Int,
		val credits: Int
	) {
		val weighted: Long = grade.toLong() * credits
	}

	private class CodeState {
		private var latest: CodeAttempt? = null
		private var second: CodeAttempt? = null

		var weightedSum: Long = 0L
			private set

		var creditsSum: Long = 0L
			private set

		val effectiveWeighted: Long
			get() {
				val currentLatest = latest
				val currentSecond = second

				return when {
					currentLatest == null -> 0L
					currentSecond == null -> weightedSum
					currentLatest.grade >= 3 -> weightedSum - currentSecond.weighted
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
					currentLatest.grade >= 3 -> creditsSum - currentSecond.credits
					else -> creditsSum
				}
			}

		fun add(attempt: CodeAttempt) {
			second = latest
			latest = attempt

			weightedSum += attempt.weighted
			creditsSum += attempt.credits
		}
	}

	fun recompute(
		quarters: List<LocalQuarter>,
		affectedStartDate: Long
	): RecomputeResult {
		if (quarters.isEmpty()) {
			return RecomputeResult(
				quarters = emptyList(),
				affectedQuarters = emptyList()
			)
		}

		/*
		 * Ascending traversal to build cumulative state once.
		 * For same start date, reverse id order to preserve deterministic recency rule:
		 * (startDate DESC, quarterId, subjectId).
		 */
		val quartersAscending = quarters.sortedWith(
			compareBy<LocalQuarter> { it.startDate }
				.thenByDescending { it.id }
		)

		val codeStates = hashMapOf<String, CodeState>()
		val recomputedAscending = ArrayList<LocalQuarter>(quartersAscending.size)

		var cumulativeWeighted = 0L
		var cumulativeCredits = 0L

		quartersAscending.forEach { quarter ->
			var quarterCredits = 0L
			var quarterWeighted = 0L

			quarter.subjects.forEach { subject ->
				if (subject.grade != 0) {
					quarterCredits += subject.credits
				}

				quarterWeighted += subject.grade.toLong() * subject.credits
			}
			val quarterGrade = computeAverage(
				weighted = quarterWeighted,
				credits = quarterCredits
			)

			val sortedSubjects = if (quarter.subjects.size > 1)
				quarter.subjects.sortedByDescending(LocalSubject::id)
			else
				quarter.subjects

			sortedSubjects.forEach { subject ->
				if (subject.grade <= 0) return@forEach

				val state = codeStates.getOrPut(subject.code, ::CodeState)

				val previousWeighted = state.effectiveWeighted
				val previousCredits = state.effectiveCredits

				state.add(
					CodeAttempt(
						grade = subject.grade,
						credits = subject.credits
					)
				)

				cumulativeWeighted += (state.effectiveWeighted - previousWeighted)
				cumulativeCredits += (state.effectiveCredits - previousCredits)
			}

			recomputedAscending += quarter.copy(
				grade = quarterGrade,
				gradeSum = computeAverage(
					weighted = cumulativeWeighted,
					credits = cumulativeCredits
				),
				credits = quarterCredits.toInt(),
				creditsSum = cumulativeCredits.toInt()
			)
		}

		/*
		 * Reverse traversal preserves:
		 * startDate DESC and, on ties, quarterId ASC
		 * because ascending order used startDate ASC + quarterId DESC.
		 */
		val quartersDescending = recomputedAscending.asReversed()

		return RecomputeResult(
			quarters = quartersDescending,
			affectedQuarters = quartersDescending.filter { quarter ->
				quarter.startDate >= affectedStartDate
			}
		)
	}

	private fun computeAverage(weighted: Long, credits: Long): Double {
		if (credits == 0L) return 0.0

		return (weighted.toDouble() / credits.toDouble())
			.round(4)
	}
}