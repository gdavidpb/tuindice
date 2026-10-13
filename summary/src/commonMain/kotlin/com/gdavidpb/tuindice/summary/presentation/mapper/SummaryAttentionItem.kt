package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummaryAttention
import com.gdavidpb.tuindice.summary.presentation.model.SummaryAttentionItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_attention_enrollment_error
import tuindice.summary.generated.resources.summary_attention_ungraded_many
import tuindice.summary.generated.resources.summary_attention_ungraded_one

private val whitespace = Regex("\\s+")

// Written in capitals whatever the sentence: the names the university goes by.
private val acronyms = setOf("DACE", "DST", "USB", "UC")

fun SummaryAttention.toSummaryAttentionItem(): SummaryAttentionItem {
	return when (this) {
		is SummaryAttention.UngradedEvaluations -> SummaryAttentionItem(
			key = "ungraded",
			text = if (count == 1) {
				UiText.Resource(Res.string.summary_attention_ungraded_one)
			} else {
				UiText.Resource(Res.string.summary_attention_ungraded_many, listOf(count))
			},
			target = SummaryHubTarget.EvaluationsWeek(weekStart = oldestWeekStart)
		)

		is SummaryAttention.EnrollmentError -> SummaryAttentionItem(
			key = "enrollment-$subjectCode",
			text = UiText.Resource(
				Res.string.summary_attention_enrollment_error,
				listOf(error.toSentenceCase(), subjectCode)
			),
			target = SummaryHubTarget.Record
		)
	}
}

/**
 * The university prints its errors in capitals: "CHOQUE DE HORARIO" reads "Choque de horario". What
 * must stay in capitals (a subject code, its acronyms) does.
 */
internal fun String.toSentenceCase(): String {
	return trim()
		.split(whitespace)
		.joinToString(separator = " ") { word ->
			val core = word.filter(Char::isLetterOrDigit).uppercase()

			if (core.any(Char::isDigit) || core in acronyms) word.uppercase() else word.lowercase()
		}
		.replaceFirstChar { first -> first.uppercase() }
}
