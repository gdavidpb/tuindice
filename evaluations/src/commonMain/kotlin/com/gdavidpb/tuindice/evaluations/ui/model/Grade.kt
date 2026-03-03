package com.gdavidpb.tuindice.evaluations.ui.model

import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

const val MIN_EVALUATION_GRADE = 0.0
const val MAX_EVALUATION_GRADE = 100.0
const val STEP_DECIMALS = 25
const val decimalSeparator = ","

fun getLoopingIndex(currentValue: Int, additionalItemCount: Int, values: List<Int>): Int {
	val startIndex = values.indexOf(currentValue)
	val count = values.size
	val half = (Int.MAX_VALUE / 2) - (count / 2)

	return half - (half % count) + startIndex - additionalItemCount
}

fun Double.toGradeWheelValue() =
	GradeWheelValue(
		int = int(),
		decimal = decimal()
	)

fun Double.int() =
	(this * 100).roundToInt() / 100

fun Double.decimal() =
	(this * 100).roundToInt() % 100

fun GradeWheelValue.toGrade() =
	int + (decimal / 100.0)

fun ClosedFloatingPointRange<Double>.computeInts(): List<Int> {
	return (start.int()..endInclusive.int()).toList()
}

fun ClosedFloatingPointRange<Double>.computeDecimals(): List<Int> {
	val d = (endInclusive - start)
	val steps = ceil(d / (STEP_DECIMALS / 100.0)).roundToInt()

	return (0 until min(steps, 100 / STEP_DECIMALS)).map { i -> i * STEP_DECIMALS }
}
