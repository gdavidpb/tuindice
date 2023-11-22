package com.gdavidpb.tuindice.record.utils.extensions

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_RETIRED
import com.gdavidpb.tuindice.base.utils.STATUS_SUBJECT_OK
import com.gdavidpb.tuindice.base.utils.extension.round

fun Collection<Subject>.removeNoEffect(): Collection<Subject> {
	val containsNoEffect = size > 1 && first().grade >= 3

	return if (containsNoEffect)
		toMutableList().apply { removeAt(1) }
	else
		this
}

fun Collection<Subject>.computeCredits() = sumOf {
	if (it.grade != 0) it.credits else 0
}

fun Collection<Subject>.computeGrade(): Double {
	val creditsSum = computeCredits().toDouble()

	val weightedSum = sumOf {
		it.grade * it.credits
	}.toDouble()

	val grade = if (creditsSum != 0.0) weightedSum / creditsSum else 0.0

	return grade.round(4)
}

fun Collection<Quarter>.computeGradeSum(until: Quarter = first()) =
	filter { it.startDate <= until.startDate && it.status != STATUS_QUARTER_RETIRED }
		/* Get all subjects */
		.flatMap { it.subjects }
		/* Filter valid subjects */
		.filter { it.status == STATUS_SUBJECT_OK }
		/* Group by code */
		.groupBy { it.code }
		/* If you've seen this subject more than once and now you approved this */
		.flatMap { (_, subjects) -> subjects.removeNoEffect() }
		/* Compute grade */
		.computeGrade()