package com.gdavidpb.tuindice.record.presentation.mapper

import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import androidx.core.text.buildSpannedString
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.STATUS_SUBJECT_NO_EFFECT
import com.gdavidpb.tuindice.base.utils.STATUS_SUBJECT_RETIRED
import com.gdavidpb.tuindice.base.utils.extension.append
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem
import com.gdavidpb.tuindice.record.utils.MIN_SUBJECT_GRADE

fun Subject.toSubjectItem(quarter: Quarter, resourceResolver: ResourceResolver) = SubjectItem(
	uid = id.hashCode().toLong(),
	id = id,
	code = code,
	isRetired = isRetired(quarter),
	nameText = name,
	codeText = spanSubjectCode(quarter, resourceResolver),
	gradeText = grade.formatGrade(resourceResolver),
	creditsText = credits.formatCredits(resourceResolver),
	data = this
)

private fun Subject.isRetired(quarter: Quarter) =
	(quarter.isEditable() && grade == MIN_SUBJECT_GRADE) ||
			(!quarter.isEditable() && status == STATUS_SUBJECT_RETIRED)

private fun Subject.spanSubjectCode(
	quarter: Quarter,
	resourceResolver: ResourceResolver
): CharSequence {
	val statusText = formatSubjectStatusDescription(quarter)

	return if (statusText.isEmpty())
		code
	else buildSpannedString {
		val content = resourceResolver.getString(R.string.subject_title, code, statusText)
		val colorSecondary = resourceResolver.getColor(R.color.color_secondary_text)

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

private fun Int.formatGrade(resourceResolver: ResourceResolver) =
	if (this != MIN_SUBJECT_GRADE)
		resourceResolver.getString(R.string.subject_grade, this)
	else
		"—"

private fun Int.formatCredits(resourceResolver: ResourceResolver) =
	resourceResolver.getString(R.string.subject_credits, this)