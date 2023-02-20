package com.gdavidpb.tuindice.record.presentation.mapper

import android.content.Context
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import androidx.core.text.buildSpannedString
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_COMPLETED
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_CURRENT
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_MOCK
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_RETIRED
import com.gdavidpb.tuindice.base.utils.extension.append
import com.gdavidpb.tuindice.base.utils.extension.getCompatColor
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem

fun Quarter.toQuarterItem(context: Context): QuarterItem {
	val quarterColor = context.getCompatColor(status.toQuarterColor())

	return QuarterItem(
		uid = id.hashCode().toLong(),
		id = id,
		color = quarterColor,
		isMock = (status == STATUS_QUARTER_MOCK),
		isEditable = isEditable(),
		TitleText = name,
		isCurrent = (status == STATUS_QUARTER_CURRENT),
		gradeDiffText = grade.formatGradeDiff(quarterColor, context),
		gradeSumText = gradeSum.formatGradeSum(quarterColor, context),
		creditsText = credits.formatCredits(quarterColor, context),
		subjectsItems = subjects.map { it.toSubjectItem(this, context) },
		data = this
	)
}

fun Quarter.isEditable() =
	(status == STATUS_QUARTER_CURRENT) || (status == STATUS_QUARTER_MOCK)

private fun Int.toQuarterColor() = when (this) {
	STATUS_QUARTER_CURRENT -> R.color.quarter_current
	STATUS_QUARTER_COMPLETED -> R.color.quarter_completed
	STATUS_QUARTER_MOCK -> R.color.quarter_mock
	STATUS_QUARTER_RETIRED -> R.color.quarter_retired
	else -> throw IllegalArgumentException("toQuarterColor: '$this'")
}

private fun String.spanGrade(color: Int): Spanned {
	val (iconString, valueString, extraString) = split(' ')
		.toMutableList()
		.apply { if (size == 2) add("") }

	return buildSpannedString {
		append(
			iconString,
			AbsoluteSizeSpan(18, true),
			ForegroundColorSpan(color)
		)
		append(' ')
		append(valueString)

		if (extraString.isNotBlank()) {
			append(' ')
			append(extraString)
		}
	}
}

private fun Double.formatGradeDiff(color: Int, context: Context) =
	context.getString(R.string.quarter_grade_diff, this).spanGrade(color)

private fun Double.formatGradeSum(color: Int, context: Context) =
	context.getString(R.string.quarter_grade_sum, this).spanGrade(color)

private fun Int.formatCredits(color: Int, context: Context) =
	context.getString(R.string.quarter_credits, this).spanGrade(color)