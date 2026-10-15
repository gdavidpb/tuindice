package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.SubjectOutcomes
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFactItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsOutcome
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSegmentItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSubjectsItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_statistics_subjects_approved_many
import tuindice.summary.generated.resources.summary_statistics_subjects_approved_one
import tuindice.summary.generated.resources.summary_statistics_subjects_empty
import tuindice.summary.generated.resources.summary_statistics_subjects_failed_many
import tuindice.summary.generated.resources.summary_statistics_subjects_failed_one
import tuindice.summary.generated.resources.summary_statistics_subjects_figure_many
import tuindice.summary.generated.resources.summary_statistics_subjects_figure_one
import tuindice.summary.generated.resources.summary_statistics_subjects_first_try
import tuindice.summary.generated.resources.summary_statistics_subjects_no_result
import tuindice.summary.generated.resources.summary_statistics_subjects_repeated_many
import tuindice.summary.generated.resources.summary_statistics_subjects_repeated_one
import tuindice.summary.generated.resources.summary_statistics_subjects_share_approved_many
import tuindice.summary.generated.resources.summary_statistics_subjects_share_approved_one
import tuindice.summary.generated.resources.summary_statistics_subjects_share_failed_many
import tuindice.summary.generated.resources.summary_statistics_subjects_share_failed_one
import tuindice.summary.generated.resources.summary_statistics_subjects_share_withdrawn_many
import tuindice.summary.generated.resources.summary_statistics_subjects_share_withdrawn_one
import tuindice.summary.generated.resources.summary_statistics_subjects_title
import tuindice.summary.generated.resources.summary_statistics_subjects_withdrawn_many
import tuindice.summary.generated.resources.summary_statistics_subjects_withdrawn_one

// "Materias": every subject of the closed terms, once by its code. The figure counts all of them. The stacked bar holds
// the approved, the failed and the withdrawn, so its whole is the subjects with a result, and its shares are out of it.
// The facts under the legend count what the bar cannot: the approvals on the first attempt, out of the subjects with a
// result, the repeated subjects, and the subjects without a result.
internal fun subjectsItemOf(outcomes: SubjectOutcomes): SummaryStatisticsSubjectsItem {
	val title = UiText.Resource(Res.string.summary_statistics_subjects_title)
	val figure = outcomes.approved + outcomes.failed + outcomes.withdrawn + outcomes.noResult

	if (figure == 0) {
		return SummaryStatisticsSubjectsItem(
			title = title,
			content = SummaryStatisticsSubjectsItem.Content.Empty(
				text = UiText.Resource(Res.string.summary_statistics_subjects_empty)
			)
		)
	}

	val whole = outcomes.approved + outcomes.failed + outcomes.withdrawn

	val shown = SummaryStatisticsFigureItem(
		value = figure.toString(),
		label = countText(
			quantity = figure,
			one = Res.string.summary_statistics_subjects_figure_one,
			many = Res.string.summary_statistics_subjects_figure_many,
			args = emptyList()
		)
	)

	return SummaryStatisticsSubjectsItem(
		title = title,
		content = SummaryStatisticsSubjectsItem.Content.Data(
			figure = shown,
			segments = segmentsOf(outcomes = outcomes, whole = whole),
			facts = factsOf(outcomes = outcomes),
			chartSummary = chartSummaryOf(shown)
		)
	)
}

private fun segmentsOf(outcomes: SubjectOutcomes, whole: Int): List<SummaryStatisticsSegmentItem> {
	return listOf(
		countSegmentOf(
			outcome = SummaryStatisticsOutcome.APPROVED,
			count = outcomes.approved,
			whole = whole,
			legend = Res.string.summary_statistics_subjects_approved_one to
				Res.string.summary_statistics_subjects_approved_many,
			share = Res.string.summary_statistics_subjects_share_approved_one to
				Res.string.summary_statistics_subjects_share_approved_many
		),
		countSegmentOf(
			outcome = SummaryStatisticsOutcome.FAILED,
			count = outcomes.failed,
			whole = whole,
			legend = Res.string.summary_statistics_subjects_failed_one to
				Res.string.summary_statistics_subjects_failed_many,
			share = Res.string.summary_statistics_subjects_share_failed_one to
				Res.string.summary_statistics_subjects_share_failed_many
		),
		countSegmentOf(
			outcome = SummaryStatisticsOutcome.WITHDRAWN,
			count = outcomes.withdrawn,
			whole = whole,
			legend = Res.string.summary_statistics_subjects_withdrawn_one to
				Res.string.summary_statistics_subjects_withdrawn_many,
			share = Res.string.summary_statistics_subjects_share_withdrawn_one to
				Res.string.summary_statistics_subjects_share_withdrawn_many
		)
	)
}

// The compact figures under the legend, each with its short label: the approvals on the first attempt, out of the
// subjects with a result, when there are any such; the repeated subjects, zero included; and the subjects without a
// result, when there are some.
private fun factsOf(outcomes: SubjectOutcomes): List<SummaryStatisticsFactItem> {
	return buildList {
		// Out of the subjects with a result, so with none there is nothing to say of the first attempt.
		if (outcomes.firstTryBase > 0) {
			add(
				SummaryStatisticsFactItem(
					value = "${outcomes.approvedFirstTry} / ${outcomes.firstTryBase}",
					label = UiText.Resource(Res.string.summary_statistics_subjects_first_try)
				)
			)
		}

		add(
			SummaryStatisticsFactItem(
				value = outcomes.repeated.toString(),
				label = countText(
					quantity = outcomes.repeated,
					one = Res.string.summary_statistics_subjects_repeated_one,
					many = Res.string.summary_statistics_subjects_repeated_many,
					args = emptyList()
				)
			)
		)

		if (outcomes.noResult > 0) {
			add(
				SummaryStatisticsFactItem(
					value = outcomes.noResult.toString(),
					label = UiText.Resource(Res.string.summary_statistics_subjects_no_result)
				)
			)
		}
	}
}
