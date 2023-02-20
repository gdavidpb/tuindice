package com.gdavidpb.tuindice.record.presentation.mapper

import android.content.Context
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import androidx.core.text.buildSpannedString
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.base.utils.STATUS_SUBJECT_NO_EFFECT
import com.gdavidpb.tuindice.base.utils.STATUS_SUBJECT_RETIRED
import com.gdavidpb.tuindice.base.utils.extension.append
import com.gdavidpb.tuindice.base.utils.extension.getCompatColor
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem
import com.gdavidpb.tuindice.record.utils.MIN_SUBJECT_GRADE

fun Subject.toSubjectItem(quarter: Quarter, context: Context) = SubjectItem(
	uid = id.hashCode().toLong(),
	id = id,
	code = code,
	isRetired = isRetired(quarter),
	nameText = name,
	codeText = spanSubjectCode(quarter, context),
	gradeText = grade.formatGrade(context),
	creditsText = credits.formatCredits(context),
	data = this
)

private fun Subject.isRetired(quarter: Quarter) =
	(quarter.isEditable() && grade == MIN_SUBJECT_GRADE) ||
			(!quarter.isEditable() && status == STATUS_SUBJECT_RETIRED)

private fun Subject.spanSubjectCode(quarter: Quarter, context: Context): CharSequence {
	val statusText = formatSubjectStatusDescription(quarter)

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

private fun Subject.formatSubjectStatusDescription(quarter: Quarter) = when {
	isRetired(quarter) -> "Retirada"
	status == STATUS_SUBJECT_NO_EFFECT -> "Sin efecto"
	else -> ""
}

private fun Int.formatGrade(context: Context) =
	if (this != MIN_SUBJECT_GRADE)
		context.getString(R.string.subject_grade, this)
	else
		"—"

private fun Int.formatCredits(context: Context) =
	context.getString(R.string.subject_credits, this)