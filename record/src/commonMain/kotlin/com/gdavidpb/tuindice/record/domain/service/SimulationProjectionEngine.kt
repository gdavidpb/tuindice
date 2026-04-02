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

	fun recompute(quarters: List<LocalQuarter>): List<LocalQuarter> {
		if (quarters.isEmpty()) return emptyList()

		val quartersAscending = quarters.sortedWith(
			compareBy<LocalQuarter> { quarter -> quarter.startDate }
				.thenByDescending { quarter -> quarter.id }
		)
		val excludedSubjectIds = resolveExcludedSubjectIds(quartersAscending)

		var cumulativeWeighted = 0L
		var cumulativeCredits = 0L

		val recomputedAscending = quartersAscending.map { quarter ->
			var quarterWeighted = 0L
			var quarterCredits = 0L

			val recomputedSubjects = quarter.subjects.map { subject ->
				val simulationStatus = when {
					subject.status != SubjectStatus.NORMAL -> null
					subject.id in excludedSubjectIds -> SubjectStatus.WITHOUT_EFFECT
					else -> null
				}

				if (
					(subject.grade > 0) &&
					(simulationStatus != SubjectStatus.WITHOUT_EFFECT)
				) {
					val weighted = subject.grade.toLong() * subject.credits.toLong()
					quarterWeighted += weighted
					quarterCredits += subject.credits.toLong()
					cumulativeWeighted += weighted
					cumulativeCredits += subject.credits.toLong()
				}

				subject.copy(simulationStatus = simulationStatus)
			}

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
