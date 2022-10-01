package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_RETIRED
import com.gdavidpb.tuindice.base.utils.STATUS_SUBJECT_OK
import com.gdavidpb.tuindice.base.utils.STATUS_SUBJECT_RETIRED
import java.util.*
import kotlin.math.roundToInt

fun Double.toSubjectGrade() = when (roundToInt()) {
    in 30 until 50 -> 2
    in 50 until 70 -> 3
    in 70 until 85 -> 4
    in 85..Integer.MAX_VALUE -> 5
    else -> 1
}

fun Int.toSubjectStatus() = if (this != 0) STATUS_SUBJECT_OK else STATUS_SUBJECT_RETIRED

fun Collection<Subject>.filterNoEffect(): Collection<Subject> {
    val containsNoEffect = size > 1 && first().grade >= 3

    return if (containsNoEffect)
        filterIndexed { index, _ -> index != 1 }
    else
        this
}

fun Date.isUpdated(): Boolean {
    val now = Date()
    val outdated = tomorrow()

    return now.before(outdated)
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
                .filter { it.status != STATUS_SUBJECT_RETIRED }
                /* Group by code */
                .groupBy { it.code }
                /* If you've seen this subject more than once and now you approved this */
                .flatMap { (_, subjects) -> subjects.filterNoEffect() }
                /* Compute grade */
                .computeGrade()