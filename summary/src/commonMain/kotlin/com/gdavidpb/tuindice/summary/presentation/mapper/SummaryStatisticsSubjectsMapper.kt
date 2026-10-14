package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.SubjectOutcomes
import com.gdavidpb.tuindice.base.presentation.model.UiText
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
import tuindice.summary.generated.resources.summary_statistics_subjects_first_try_many
import tuindice.summary.generated.resources.summary_statistics_subjects_first_try_one
import tuindice.summary.generated.resources.summary_statistics_subjects_no_repeated
import tuindice.summary.generated.resources.summary_statistics_subjects_no_result
import tuindice.summary.generated.resources.summary_statistics_subjects_repeated_many
import tuindice.summary.generated.resources.summary_statistics_subjects_repeated_one
import tuindice.summary.generated.resources.summary_statistics_subjects_share_approved_many
import tuindice.summary.generated.resources.summary_statistics_subjects_share_approved_one
import tuindice.summary.generated.resources.summary_statistics_subjects_share_failed_many
import tuindice.summary.generated.resources.summary_statistics_subjects_share_failed_one
import tuindice.summary.generated.resources.summary_statistics_subjects_share_no_result
import tuindice.summary.generated.resources.summary_statistics_subjects_share_withdrawn_many
import tuindice.summary.generated.resources.summary_statistics_subjects_share_withdrawn_one
import tuindice.summary.generated.resources.summary_statistics_subjects_title
import tuindice.summary.generated.resources.summary_statistics_subjects_withdrawn_many
import tuindice.summary.generated.resources.summary_statistics_subjects_withdrawn_one

// "Materias": every subject of the closed terms, once by its code, in one stacked bar. The whole is the sum of the four
// outcomes, so the bar fills up to the figure. The notes say how many were approved on the first attempt, out of the
// subjects with a result, and how many were repeated.
internal fun subjectsItemOf(outcomes: SubjectOutcomes): SummaryStatisticsSubjectsItem {
	val title = UiText.Resource(Res.string.summary_statistics_subjects_title)
	val whole = outcomes.approved + outcomes.failed + outcomes.withdrawn + outcomes.noResult

	if (whole == 0) {
		return SummaryStatisticsSubjectsItem(
			title = title,
			content = SummaryStatisticsSubjectsItem.Content.Empty(
				text = UiText.Resource(Res.string.summary_statistics_subjects_empty)
			)
		)
	}

	return SummaryStatisticsSubjectsItem(
		title = title,
		content = SummaryStatisticsSubjectsItem.Content.Data(
			figure = SummaryStatisticsFigureItem(
				value = whole.toString(),
				label = countText(
					quantity = whole,
					one = Res.string.summary_statistics_subjects_figure_one,
					many = Res.string.summary_statistics_subjects_figure_many,
					args = emptyList()
				)
			),
			segments = segmentsOf(outcomes = outcomes, whole = whole),
			notes = notesOf(outcomes = outcomes)
		)
	)
}

private fun segmentsOf(outcomes: SubjectOutcomes, whole: Int): List<SummaryStatisticsSegmentItem> {
	return buildList {
		add(
			countSegmentOf(
				outcome = SummaryStatisticsOutcome.APPROVED,
				count = outcomes.approved,
				whole = whole,
				legend = Res.string.summary_statistics_subjects_approved_one to
					Res.string.summary_statistics_subjects_approved_many,
				share = Res.string.summary_statistics_subjects_share_approved_one to
					Res.string.summary_statistics_subjects_share_approved_many
			)
		)
		add(
			countSegmentOf(
				outcome = SummaryStatisticsOutcome.FAILED,
				count = outcomes.failed,
				whole = whole,
				legend = Res.string.summary_statistics_subjects_failed_one to
					Res.string.summary_statistics_subjects_failed_many,
				share = Res.string.summary_statistics_subjects_share_failed_one to
					Res.string.summary_statistics_subjects_share_failed_many
			)
		)
		add(
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

		// The subjects without a result leave no legend when there are none, since they are none to count.
		if (outcomes.noResult > 0) {
			add(
				plainSegmentOf(
					outcome = SummaryStatisticsOutcome.NO_RESULT,
					count = outcomes.noResult,
					whole = whole,
					legend = Res.string.summary_statistics_subjects_no_result,
					share = Res.string.summary_statistics_subjects_share_no_result
				)
			)
		}
	}
}

private fun notesOf(outcomes: SubjectOutcomes): List<UiText> {
	return buildList {
		// Out of the subjects with a result, so with none there is nothing to say of the first attempt.
		if (outcomes.firstTryBase > 0) {
			add(
				countText(
					quantity = outcomes.firstTryBase,
					one = Res.string.summary_statistics_subjects_first_try_one,
					many = Res.string.summary_statistics_subjects_first_try_many,
					args = listOf(outcomes.approvedFirstTry, outcomes.firstTryBase)
				)
			)
		}

		add(
			if (outcomes.repeated == 0) {
				UiText.Resource(Res.string.summary_statistics_subjects_no_repeated)
			} else {
				countText(
					quantity = outcomes.repeated,
					one = Res.string.summary_statistics_subjects_repeated_one,
					many = Res.string.summary_statistics_subjects_repeated_many,
					args = listOf(outcomes.repeated)
				)
			}
		)
	}
}
