package com.gdavidpb.tuindice.evaluations.utils.extension

import kotlin.math.roundToInt

fun Double.toSubjectGrade() = when (roundToInt()) {
	in 30 until 50 -> 2
	in 50 until 70 -> 3
	in 70 until 85 -> 4
	in 85..Integer.MAX_VALUE -> 5
	else -> 1
}