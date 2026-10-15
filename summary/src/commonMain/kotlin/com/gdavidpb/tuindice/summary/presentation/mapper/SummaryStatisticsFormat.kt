package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import org.jetbrains.compose.resources.StringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_percent_format
import tuindice.summary.generated.resources.summary_statistics_chart_summary
import kotlin.math.roundToInt

private const val PERCENT = 100

// The decimal places of an index and of a cumulative average, as the statistics and the share cards write them.
internal const val INDEX_DECIMALS = 4

// The text of a quantity, in the singular for one and in the plural for any other number, as the summary's other counts
// read. [quantity] picks the form, and [args] are what the form takes, in order.
internal fun countText(quantity: Int, one: StringResource, many: StringResource, args: List<Any>): UiText {
	return UiText.Resource(if (quantity == 1) one else many, args)
}

// A part of a whole as a whole percentage, rounded the way the screens round it.
internal fun percentOf(part: Int, whole: Int): Int {
	return if (whole == 0) 0 else (part * PERCENT.toDouble() / whole).roundToInt()
}

// A part of a whole as a share: "4 %". The number is the only argument, and the format escapes its percent sign, so
// the text is built by the resources like every other.
internal fun shareOf(part: Int, whole: Int): UiText = percentText(percentOf(part = part, whole = whole))

// What a screen reader hears of a chart that its figure leads: the figure's value and what it counts, as one phrase.
internal fun chartSummaryOf(figure: SummaryStatisticsFigureItem): UiText {
	return UiText.Resource(Res.string.summary_statistics_chart_summary, listOf(figure.value, figure.label))
}

// A fraction of the whole as a share, rounded the same way.
internal fun shareOf(fraction: Double): UiText = percentText((fraction * PERCENT).roundToInt())

private fun percentText(percent: Int): UiText = UiText.Resource(Res.string.summary_percent_format, listOf(percent))

// Points the way the Evaluations tab shows them: a whole number without a decimal place, any other with one.
internal fun Double.formatPoints(): String {
	val whole = toInt()

	return if (this == whole.toDouble()) whole.toString() else formatGrade(decimals = 1)
}
