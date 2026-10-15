package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.TermStatistic
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummaryCreditsProgress
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCareerItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCreditsItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_statistics_credits_empty
import tuindice.summary.generated.resources.summary_statistics_credits_invite_message
import tuindice.summary.generated.resources.summary_statistics_credits_title

// "Créditos": the credits the pensum has approved, with their share and the bar that follows them. The figures are the
// career card's, so the two blocks agree. Until a term is closed there is nothing to count, and the block says when its
// credits will appear. Without a pensum to measure against, it invites to open one.
internal fun creditsItemOf(
	closedTerms: List<TermStatistic>,
	pensum: SummaryCreditsProgress?
): SummaryStatisticsCreditsItem {
	val title = UiText.Resource(Res.string.summary_statistics_credits_title)

	if (closedTerms.isEmpty()) {
		return SummaryStatisticsCreditsItem(
			title = title,
			content = SummaryStatisticsCreditsItem.Content.Empty(
				text = UiText.Resource(Res.string.summary_statistics_credits_empty)
			)
		)
	}

	return SummaryStatisticsCreditsItem(
		title = title,
		content = contentOf(career = pensum.toSummaryCareerItem())
	)
}

private fun contentOf(career: SummaryCareerItem): SummaryStatisticsCreditsItem.Content {
	return when (career) {
		is SummaryCareerItem.Progress -> SummaryStatisticsCreditsItem.Content.Data(
			figure = SummaryStatisticsFigureItem(
				value = career.valueText,
				label = career.restText,
				trailing = career.percentText
			),
			fraction = career.fraction,
			description = career.description
		)

		SummaryCareerItem.Invite -> SummaryStatisticsCreditsItem.Content.Empty(
			text = UiText.Resource(Res.string.summary_statistics_credits_invite_message)
		)
	}
}
