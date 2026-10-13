package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.utils.extension.toSentenceCase
import com.gdavidpb.tuindice.summary.domain.model.SummaryAttention
import com.gdavidpb.tuindice.summary.presentation.model.SummaryAttentionItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_attention_enrollment_error
import tuindice.summary.generated.resources.summary_attention_ungraded_many
import tuindice.summary.generated.resources.summary_attention_ungraded_one

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
				listOf(error.withoutFinalPunctuation().toSentenceCase(), subjectCode)
			),
			target = SummaryHubTarget.Record
		)
	}
}

// The university ends its messages with a period: "... . en CI5311" would read as two sentences.
private fun String.withoutFinalPunctuation(): String {
	return trimEnd { char -> char.isWhitespace() || char in FINAL_PUNCTUATION }
}

private const val FINAL_PUNCTUATION = ".,;:!…"
