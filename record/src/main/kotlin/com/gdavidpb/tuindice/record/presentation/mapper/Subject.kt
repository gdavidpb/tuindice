package com.gdavidpb.tuindice.record.presentation.mapper

import android.content.Context
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import androidx.core.text.buildSpannedString
import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.base.utils.STATUS_SUBJECT_NO_EFFECT
import com.gdavidpb.tuindice.base.utils.STATUS_SUBJECT_RETIRED
import com.gdavidpb.tuindice.base.utils.extension.append
import com.gdavidpb.tuindice.base.utils.extension.getCompatColor
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem

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

fun Int.formatSubjectStatusDescription() = when (this) {
	STATUS_SUBJECT_RETIRED -> "Retirada"
	STATUS_SUBJECT_NO_EFFECT -> "Sin efecto"
	else -> ""
}

fun Int.formatGrade(context: Context) =
	if (this != 0)
		context.getString(R.string.subject_grade, this)
	else
		"—"

fun Int.formatCredits(context: Context) =
	context.getString(R.string.subject_credits, this)