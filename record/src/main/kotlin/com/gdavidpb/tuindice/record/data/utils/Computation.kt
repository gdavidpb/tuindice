package com.gdavidpb.tuindice.record.data.utils

import com.gdavidpb.tuindice.base.utils.extension.round
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject

fun Collection<LocalSubject>.removeNoEffect(): Collection<LocalSubject> {
	val containsNoEffect = size > 1 && first().grade >= 3

	return if (containsNoEffect)
		toMutableList().apply { removeAt(1) }
	else
		this
}

fun Collection<LocalSubject>.computeCredits() = sumOf {
	if (it.grade != 0) it.credits else 0
}

fun Collection<LocalSubject>.computeGrade(): Double {
	val creditsSum = computeCredits().toDouble()

	val weightedSum = sumOf {
		it.grade * it.credits
	}.toDouble()

	val grade = if (creditsSum != 0.0) weightedSum / creditsSum else 0.0

	return grade.round(4)
}

fun Collection<LocalQuarter>.computeGradeSum(until: LocalQuarter = first()) =
	asSequence()
		.filter { it.startDate <= until.startDate }
		/* Get all subjects */
		.flatMap { it.subjects }
		/* Filter valid subjects */
		.filter { it.grade > 0 }
		/* Group by code */
		.groupBy { it.code }
		/* If you've seen this subject more than once and now you approved this */
		.flatMap { (_, subjects) -> subjects.removeNoEffect() }.toList()
		/* Compute grade */
		.computeGrade()