package com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils

import com.gdavidpb.tuindice.base.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.model.GradeWheelValue
import java.text.DecimalFormatSymbols
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

internal const val MIN_EVALUATION_GRADE = 0.0
internal const val MAX_EVALUATION_GRADE = 100.0
internal const val STEP_DECIMALS = 25

internal val decimalSeparator = DecimalFormatSymbols
	.getInstance(DEFAULT_LOCALE)
	.decimalSeparator
	.toString()

internal fun getLoopingIndex(currentValue: Int, additionalItemCount: Int, values: List<Int>): Int {
	val startIndex = values.indexOf(currentValue)
	val count = values.size
	val half = (Int.MAX_VALUE / 2) - (count / 2)

	return half - (half % count) + startIndex - additionalItemCount
}

internal fun Double.toGradeWheelValue() =
	GradeWheelValue(
		int = int(),
		decimal = decimal()
	)

internal fun Double.int() =
	(this * 100).roundToInt() / 100

internal fun Double.decimal() =
	(this * 100).roundToInt() % 100

internal fun GradeWheelValue.toGrade() =
	int + (decimal / 100.0)

internal fun ClosedFloatingPointRange<Double>.computeInts(): List<Int> {
	return (start.int()..endInclusive.int()).toList()
}

internal fun ClosedFloatingPointRange<Double>.computeDecimals(): List<Int> {
	val d = (endInclusive - start)
	val steps = ceil(d / (STEP_DECIMALS / 100.0)).roundToInt()

	return (0 until min(steps, 100 / STEP_DECIMALS)).map { i -> i * STEP_DECIMALS }
}