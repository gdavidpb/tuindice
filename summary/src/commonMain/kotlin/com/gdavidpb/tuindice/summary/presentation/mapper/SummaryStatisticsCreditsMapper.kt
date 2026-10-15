package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.CreditOutcomes
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummaryCreditsProgress
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCreditsItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsOutcome
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSegmentItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_career_percent
import tuindice.summary.generated.resources.summary_statistics_credits_approved_many
import tuindice.summary.generated.resources.summary_statistics_credits_approved_one
import tuindice.summary.generated.resources.summary_statistics_credits_empty
import tuindice.summary.generated.resources.summary_statistics_credits_failed_many
import tuindice.summary.generated.resources.summary_statistics_credits_failed_one
import tuindice.summary.generated.resources.summary_statistics_credits_figure_many
import tuindice.summary.generated.resources.summary_statistics_credits_figure_one
import tuindice.summary.generated.resources.summary_statistics_credits_pensum
import tuindice.summary.generated.resources.summary_statistics_credits_share_approved_many
import tuindice.summary.generated.resources.summary_statistics_credits_share_approved_one
import tuindice.summary.generated.resources.summary_statistics_credits_share_failed_many
import tuindice.summary.generated.resources.summary_statistics_credits_share_failed_one
import tuindice.summary.generated.resources.summary_statistics_credits_share_withdrawn_many
import tuindice.summary.generated.resources.summary_statistics_credits_share_withdrawn_one
import tuindice.summary.generated.resources.summary_statistics_credits_title
import tuindice.summary.generated.resources.summary_statistics_credits_withdrawn_many
import tuindice.summary.generated.resources.summary_statistics_credits_withdrawn_one

// "Créditos": the credits of the closed terms by outcome, each subject's credits counted once. The pensum's own
// progress comes after the bar, when a pensum is cached. With no credit from the record there is nothing to show, so
// the block says what it lacks and leaves the pensum out with it.
internal fun creditsItemOf(
	outcomes: CreditOutcomes,
	pensum: SummaryCreditsProgress?
): SummaryStatisticsCreditsItem {
	val title = UiText.Resource(Res.string.summary_statistics_credits_title)
	val whole = outcomes.approved + outcomes.failed + outcomes.withdrawn

	if (whole == 0) {
		return SummaryStatisticsCreditsItem(
			title = title,
			content = SummaryStatisticsCreditsItem.Content.Empty(
				text = UiText.Resource(Res.string.summary_statistics_credits_empty)
			)
		)
	}

	val figure = SummaryStatisticsFigureItem(
		value = whole.toString(),
		label = countText(
			quantity = whole,
			one = Res.string.summary_statistics_credits_figure_one,
			many = Res.string.summary_statistics_credits_figure_many,
			args = emptyList()
		)
	)

	return SummaryStatisticsCreditsItem(
		title = title,
		content = SummaryStatisticsCreditsItem.Content.Data(
			figure = figure,
			segments = segmentsOf(outcomes = outcomes, whole = whole),
			pensum = pensumOf(progress = pensum),
			chartSummary = chartSummaryOf(figure)
		)
	)
}

private fun segmentsOf(outcomes: CreditOutcomes, whole: Int): List<SummaryStatisticsSegmentItem> {
	return listOf(
		countSegmentOf(
			outcome = SummaryStatisticsOutcome.APPROVED,
			count = outcomes.approved,
			whole = whole,
			legend = Res.string.summary_statistics_credits_approved_one to
				Res.string.summary_statistics_credits_approved_many,
			share = Res.string.summary_statistics_credits_share_approved_one to
				Res.string.summary_statistics_credits_share_approved_many
		),
		countSegmentOf(
			outcome = SummaryStatisticsOutcome.FAILED,
			count = outcomes.failed,
			whole = whole,
			legend = Res.string.summary_statistics_credits_failed_one to
				Res.string.summary_statistics_credits_failed_many,
			share = Res.string.summary_statistics_credits_share_failed_one to
				Res.string.summary_statistics_credits_share_failed_many
		),
		countSegmentOf(
			outcome = SummaryStatisticsOutcome.WITHDRAWN,
			count = outcomes.withdrawn,
			whole = whole,
			legend = Res.string.summary_statistics_credits_withdrawn_one to
				Res.string.summary_statistics_credits_withdrawn_many,
			share = Res.string.summary_statistics_credits_share_withdrawn_one to
				Res.string.summary_statistics_credits_share_withdrawn_many
		)
	)
}

// The pensum's own progress, out of its total: the approved credits, the share of them and the track's fraction. Null
// when no pensum is cached, or when it has no credits to be out of.
private fun pensumOf(progress: SummaryCreditsProgress?): SummaryStatisticsCreditsItem.Pensum? {
	if (progress == null || progress.totalCredits <= 0) return null

	return SummaryStatisticsCreditsItem.Pensum(
		figure = SummaryStatisticsFigureItem(
			value = progress.approvedCredits.toString(),
			label = UiText.Resource(
				Res.string.summary_statistics_credits_pensum,
				listOf(progress.totalCredits)
			),
			trailing = UiText.Resource(
				Res.string.summary_career_percent,
				listOf(percentOf(part = progress.approvedCredits, whole = progress.totalCredits))
			)
		),
		fraction = (progress.approvedCredits.toFloat() / progress.totalCredits).coerceIn(0f, 1f)
	)
}
