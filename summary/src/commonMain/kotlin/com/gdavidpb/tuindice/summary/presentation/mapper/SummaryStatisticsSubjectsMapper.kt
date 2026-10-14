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
import tuindice.summary.generated.resources.summary_statistics_subjects_no_result_many
import tuindice.summary.generated.resources.summary_statistics_subjects_no_result_one
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
// The subjects without a result are a line of their own. The notes say how many were approved on the first attempt, out
// of the subjects with a result, and how many were repeated.
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

	return SummaryStatisticsSubjectsItem(
		title = title,
		content = SummaryStatisticsSubjectsItem.Content.Data(
			figure = SummaryStatisticsFigureItem(
				value = figure.toString(),
				label = countText(
					quantity = figure,
					one = Res.string.summary_statistics_subjects_figure_one,
					many = Res.string.summary_statistics_subjects_figure_many,
					args = emptyList()
				)
			),
			segments = segmentsOf(outcomes = outcomes, whole = whole),
			notes = notesOf(outcomes = outcomes),
			noResult = noResultOf(count = outcomes.noResult)
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

// "1 materia sin resultado" or "3 materias sin resultado", and no line at all when there is none to count.
private fun noResultOf(count: Int): UiText? {
	if (count == 0) return null

	return countText(
		quantity = count,
		one = Res.string.summary_statistics_subjects_no_result_one,
		many = Res.string.summary_statistics_subjects_no_result_many,
		args = listOf(count)
	)
}
