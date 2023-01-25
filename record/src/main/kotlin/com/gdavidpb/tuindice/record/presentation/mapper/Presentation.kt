package com.gdavidpb.tuindice.record.presentation.mapper

import android.content.Context
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import androidx.core.text.buildSpannedString
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.base.utils.*
import com.gdavidpb.tuindice.base.utils.extension.append
import com.gdavidpb.tuindice.base.utils.extension.getCompatColor
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem

/* Presentation */

fun Quarter.toQuarterItem(context: Context): QuarterItem {
	val quarterColor = context.getCompatColor(status.toQuarterColor())

	return QuarterItem(
		uid = id.hashCode().toLong(),
		id = id,
		color = quarterColor,
		isMock = (status == STATUS_QUARTER_MOCK),
		isEditable = (status == STATUS_QUARTER_CURRENT) || (status == STATUS_QUARTER_MOCK),
		TitleText = name,
		isCurrent = (status == STATUS_QUARTER_CURRENT),
		gradeDiffText = grade.formatGradeDiff(quarterColor, context),
		gradeSumText = gradeSum.formatGradeSum(quarterColor, context),
		creditsText = credits.formatCredits(quarterColor, context),
		subjectsItems = subjects.map { it.toSubjectItem(context) },
		data = this
	)
}

fun Subject.toSubjectItem(context: Context) = SubjectItem(
	uid = id.hashCode().toLong(),
	id = id,
	code = code,
	isRetired = (status == STATUS_SUBJECT_RETIRED),
	nameText = name,
	codeText = spanSubjectCode(context),
	gradeText = grade.formatGrade(context),
	creditsText = credits.formatCredits(context),
	data = this
)

/* Format */

fun Int.toQuarterColor() = when (this) {
	STATUS_QUARTER_CURRENT -> R.color.quarter_current
	STATUS_QUARTER_COMPLETED -> R.color.quarter_completed
	STATUS_QUARTER_MOCK -> R.color.quarter_mock
	STATUS_QUARTER_RETIRED -> R.color.quarter_retired
	else -> throw IllegalArgumentException("toQuarterColor: '$this'")
}

fun Int.formatGrade(context: Context) =
	if (this != 0)
		context.getString(R.string.subject_grade, this)
	else
		"—"

fun Int.formatCredits(context: Context) =
	context.getString(R.string.subject_credits, this)

fun Double.formatGradeDiff(color: Int, context: Context) =
	context.getString(R.string.quarter_grade_diff, this).spanGrade(color)

fun Double.formatGradeSum(color: Int, context: Context) =
	context.getString(R.string.quarter_grade_sum, this).spanGrade(color)

fun Int.formatCredits(color: Int, context: Context) =
	context.getString(R.string.quarter_credits, this).spanGrade(color)

fun Int.formatSubjectStatusDescription() = when (this) {
	STATUS_SUBJECT_RETIRED -> "Retirada"
	STATUS_SUBJECT_NO_EFFECT -> "Sin efecto"
	else -> ""
}

/* Span */

fun Subject.spanSubjectCode(context: Context): CharSequence {
	val statusText = status.formatSubjectStatusDescription()

	return if (statusText.isEmpty())
		code
	else buildSpannedString {
		val content = context.getString(R.string.subject_title, code, statusText)
		val colorSecondary = context.getCompatColor(R.color.color_secondary_text)

		append(content.substringBefore(' '))
		append(' ')
		append(
			content.substringAfter(' '),
			TypefaceSpan("sans-serif-light"),
			ForegroundColorSpan(colorSecondary)
		)

	}
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