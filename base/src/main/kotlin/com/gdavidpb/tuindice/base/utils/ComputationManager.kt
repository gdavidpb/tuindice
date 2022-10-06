package com.gdavidpb.tuindice.base.utils

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.base.utils.extensions.computeCredits
import com.gdavidpb.tuindice.base.utils.extensions.computeGrade
import com.gdavidpb.tuindice.base.utils.extensions.computeGradeSum
import com.gdavidpb.tuindice.base.utils.extensions.toSubjectStatus

object ComputationManager {
	data class ComputationResult(
		val grade: Double,
		val gradeSum: Double,
		val credits: Int
	)

	private val quartersCache = LinkedHashMap<String, Quarter>()
	private val quartersIndexCache = hashMapOf<String, Int>()
	private val subjectsCache = hashMapOf<String, Subject>()
	private val subjectsIndexCache = hashMapOf<String, Int>()
	private val computationCache = hashMapOf<Int, ComputationResult>()

	private var lastEditableAt: Int = 0

	fun computeQuarter(quarterId: String, subjectId: String, grade: Int): Quarter {
		val quarter = getQuarterById(quarterId)
		val subject = getSubjectById(quarterId, subjectId)

		val quarterIndex = getQuarterIndexById(quarterId)
		val subjectIndex = getSubjectIndexById(quarterId, subjectId)

		val updatedSubject = subject.copy(
			grade = grade,
			status = grade.toSubjectStatus()
		)

		quarter.subjects[subjectIndex] = updatedSubject

		val subRange = quarterIndex..lastEditableAt
		val subQuarters = quartersCache.values.filterIndexed { index, _ -> index in subRange }

		val computationId = subQuarters.hashCode()

		val computationResult = computationCache.getOrPut(computationId) {
			ComputationResult(
				grade = quarter.subjects.computeGrade(),
				gradeSum = quartersCache.values.computeGradeSum(until = quarter),
				credits = quarter.subjects.computeCredits()
			)
		}

		return quarter.copy(
			grade = computationResult.grade,
			gradeSum = computationResult.gradeSum,
			credits = computationResult.credits
		)
	}

	fun setQuarters(quarters: List<Quarter>) {
		clearCache()

		quartersCache.putAll(quarters.associateBy { it.id })

		lastEditableAt = getLastEditableIndex()
	}

	fun clearCache() {
		quartersCache.clear()
		quartersIndexCache.clear()
		subjectsCache.clear()
		subjectsIndexCache.clear()
		computationCache.clear()

		lastEditableAt = 0
	}

	private fun getQuarterById(qid: String): Quarter {
		return quartersCache[qid] ?: error("quarter not found: $qid")
	}

	private fun getSubjectById(qid: String, sid: String): Subject {
		return subjectsCache.getOrPut(sid) {
			val quarter = getQuarterById(qid)

			quarter.subjects.find { it.id == sid } ?: error("subject not found: $sid")
		}
	}

	private fun getQuarterIndexById(qid: String): Int {
		return quartersIndexCache.getOrPut(qid) {
			quartersCache.values.indexOfFirst { it.id == qid }.also { index ->
				if (index == -1) error("quarter not found: $qid")
			}
		}
	}

	private fun getSubjectIndexById(qid: String, sid: String): Int {
		return subjectsIndexCache.getOrPut(sid) {
			val quarter = getQuarterById(qid)

			quarter.subjects.indexOfFirst { it.id == sid }.also { index ->
				if (index == -1) error("subject not found: $sid")
			}
		}
	}

	private fun getLastEditableIndex(): Int {
		val index = quartersCache.values.indexOfFirst {
			(it.status != STATUS_QUARTER_CURRENT) && (it.status != STATUS_QUARTER_MOCK)
		}

		return if (index != -1) index else 0
	}
}