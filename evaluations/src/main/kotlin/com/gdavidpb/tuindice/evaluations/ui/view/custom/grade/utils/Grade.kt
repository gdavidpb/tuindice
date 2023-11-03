package com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils

import com.gdavidpb.tuindice.base.ui.view.WheelPickerDefaults
import com.gdavidpb.tuindice.base.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.model.GradeWheelValue
import java.text.DecimalFormatSymbols
import kotlin.math.roundToInt

internal const val decimals_step = 25

internal val ints = (0..100).toList()
internal val decimals = (0..<100 step decimals_step).toList()

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
	(this * 100)
		.roundToInt()
		.let { grade ->
			GradeWheelValue(
				int = grade / 100,
				decimal = grade % 100
			)
		}

internal fun GradeWheelValue.toGrade() =
	int + (decimal / 100.0)