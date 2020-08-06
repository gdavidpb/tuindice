package com.gdavidpb.tuindice.utils

import com.gdavidpb.tuindice.domain.model.service.DstSubject
import kotlin.math.floor

fun Collection<DstSubject>.computeGrade(): Double {
    val creditsSum = computeCredits().toDouble()

    val weightedSum = sumBy {
        it.grade * it.credits
    }.toDouble()

    val grade = if (creditsSum != 0.0) weightedSum / creditsSum else 0.0

    return floor(grade * 10000.0) / 10000.0
}

fun Collection<DstSubject>.computeCredits() = sumBy {
    if (it.grade != 0) it.credits else 0
}