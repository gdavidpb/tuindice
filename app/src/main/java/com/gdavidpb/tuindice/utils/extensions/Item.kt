package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.presentation.model.QuarterItem

fun Collection<QuarterItem>.computeGradeSumUntil(until: QuarterItem) =
        map { it.data }.computeGradeSum(until.data)

fun QuarterItem.computeGrade() = data.subjects.computeGrade()

fun QuarterItem.computeCredits() = data.subjects.computeCredits()