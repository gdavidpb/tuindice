package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.summary.domain.model.SummaryFinishedTerm
import com.gdavidpb.tuindice.summary.presentation.model.SummaryFinishedTermItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_term_credits_label
import tuindice.summary.generated.resources.summary_term_credits_value
import tuindice.summary.generated.resources.summary_term_finished_state
import tuindice.summary.generated.resources.summary_term_index_label

// "Abr - Jul 2026" with "terminado", its index with four decimals as Registro shows it, the credits it approved,
// and the key and the share flag the share sheet is opened with.
internal fun SummaryFinishedTerm.toSummaryFinishedTermItem(): SummaryFinishedTermItem {
	return SummaryFinishedTermItem(
		nameText = UiText.Raw("${periodCode.shortLabel} $periodYear"),
		stateText = UiText.Resource(Res.string.summary_term_finished_state),
		indexLabel = UiText.Resource(Res.string.summary_term_index_label),
		indexText = index.formatGrade(decimals = INDEX_DECIMALS),
		creditsLabel = UiText.Resource(Res.string.summary_term_credits_label),
		creditsText = UiText.Resource(Res.string.summary_term_credits_value, listOf(approvedCredits)),
		termKey = termKey,
		isShareable = isShareable
	)
}
