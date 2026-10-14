package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import org.jetbrains.compose.resources.StringResource
import kotlin.math.roundToInt

private const val PERCENT = 100

// The text of a quantity, in the singular for one and in the plural for any other number, as the summary's other counts
// read. [quantity] picks the form, and [args] are what the form takes, in order.
internal fun countText(quantity: Int, one: StringResource, many: StringResource, args: List<Any>): UiText {
	return UiText.Resource(if (quantity == 1) one else many, args)
}

// A part of a whole as a whole percentage, rounded the way the screens round it.
internal fun percentOf(part: Int, whole: Int): Int {
	return if (whole == 0) 0 else (part * PERCENT.toDouble() / whole).roundToInt()
}

// A part of a whole as a share: "4 %". The percent sign is an argument and never part of a format, so a format never
// reads it as a conversion.
internal fun shareOf(part: Int, whole: Int): String = "${percentOf(part = part, whole = whole)} %"

// A fraction of the whole as a share, rounded the same way.
internal fun shareOf(fraction: Double): String = "${(fraction * PERCENT).roundToInt()} %"

// Points the way the Evaluations tab shows them: a whole number without a decimal place, any other with one.
internal fun Double.formatPoints(): String {
	val whole = toInt()

	return if (this == whole.toDouble()) whole.toString() else formatGrade(decimals = 1)
}
