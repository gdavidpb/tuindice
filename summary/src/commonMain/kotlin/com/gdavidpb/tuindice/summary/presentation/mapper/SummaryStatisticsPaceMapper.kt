package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.PaceStatistics
import com.gdavidpb.tuindice.academiccore.domain.model.TermStatistic
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsBarItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsPaceItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsReferenceItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsTooltipItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_statistics_pace_complete
import tuindice.summary.generated.resources.summary_statistics_pace_empty
import tuindice.summary.generated.resources.summary_statistics_pace_figure
import tuindice.summary.generated.resources.summary_statistics_pace_invite_message
import tuindice.summary.generated.resources.summary_statistics_pace_invite_title
import tuindice.summary.generated.resources.summary_statistics_pace_projection_emphasis_many
import tuindice.summary.generated.resources.summary_statistics_pace_projection_emphasis_one
import tuindice.summary.generated.resources.summary_statistics_pace_projection_many
import tuindice.summary.generated.resources.summary_statistics_pace_projection_one
import tuindice.summary.generated.resources.summary_statistics_pace_projection_pending
import tuindice.summary.generated.resources.summary_statistics_pace_projection_tag
import tuindice.summary.generated.resources.summary_statistics_pace_reference
import tuindice.summary.generated.resources.summary_statistics_pace_title
import tuindice.summary.generated.resources.summary_statistics_pace_tooltip_subjects_many
import tuindice.summary.generated.resources.summary_statistics_pace_tooltip_subjects_one
import tuindice.summary.generated.resources.summary_statistics_pace_tooltip_title
import com.gdavidpb.tuindice.summary.domain.model.SummaryStatistics as SummaryStatisticsModel

private const val AVERAGE_DECIMALS = 1
private const val MIN_REGULAR_TERMS_FOR_PROJECTION = 2

// "Ritmo": one bar per closed term with the credits it approved, the July - August term included. The reference line is
// the average of the regular terms, which is the only pace the projection uses. Without a regular term there is no pace
// to draw: the block says it will appear.
internal fun paceItemOf(summary: SummaryStatisticsModel, axisLabels: Map<String, String>): SummaryStatisticsPaceItem {
	val title = UiText.Resource(Res.string.summary_statistics_pace_title)
	val terms = summary.closed.terms
	val regularTerms = terms.count { term -> term.isRegular }
	val average = summary.pace.averageCredits

	if (regularTerms == 0 || average == null) {
		return SummaryStatisticsPaceItem(
			title = title,
			content = SummaryStatisticsPaceItem.Content.Empty(
				text = UiText.Resource(Res.string.summary_statistics_pace_empty)
			)
		)
	}

	return SummaryStatisticsPaceItem(
		title = title,
		content = SummaryStatisticsPaceItem.Content.Data(
			figure = SummaryStatisticsFigureItem(
				value = average.formatGrade(decimals = AVERAGE_DECIMALS),
				label = UiText.Resource(Res.string.summary_statistics_pace_figure)
			),
			bars = terms.map { term -> barOf(term = term, axisLabels = axisLabels) },
			reference = SummaryStatisticsReferenceItem(
				value = average.toFloat(),
				label = UiText.Resource(
					Res.string.summary_statistics_pace_reference,
					listOf(average.formatGrade(decimals = AVERAGE_DECIMALS))
				)
			),
			projection = projectionOf(
				pace = summary.pace,
				pensum = summary.pensumProgress,
				regularTerms = regularTerms
			)
		)
	)
}

private fun barOf(term: TermStatistic, axisLabels: Map<String, String>): SummaryStatisticsBarItem {
	return SummaryStatisticsBarItem(
		axisLabel = UiText.Raw(axisLabels.getValue(term.termKey)),
		value = term.approvedCredits.toFloat(),
		tooltip = SummaryStatisticsTooltipItem(
			title = UiText.Resource(
				Res.string.summary_statistics_pace_tooltip_title,
				listOf(term.fullLabel(), term.approvedCredits)
			),
			lines = listOf(
				countText(
					quantity = term.approvedSubjects,
					one = Res.string.summary_statistics_pace_tooltip_subjects_one,
					many = Res.string.summary_statistics_pace_tooltip_subjects_many,
					args = listOf(term.approvedSubjects)
				)
			)
		)
	)
}

// What the pensum still takes at this pace, or why it cannot say yet. A pensum that is not cached cannot be projected,
// so the block invites to open it; a complete one has nothing left to project. The domain gives no projection for
// either, so the pensum tells them apart. With two regular terms and no credit approved there is no pace to project
// from, and the block says nothing of it.
private fun projectionOf(
	pace: PaceStatistics,
	pensum: PensumCreditsProgress?,
	regularTerms: Int
): SummaryStatisticsPaceItem.Projection {
	if (pensum == null) {
		return SummaryStatisticsPaceItem.Projection.NoPensum(
			title = UiText.Resource(Res.string.summary_statistics_pace_invite_title),
			message = UiText.Resource(Res.string.summary_statistics_pace_invite_message)
		)
	}

	val remainingTerms = pace.remainingTerms
	val isComplete = pensum.totalCredits > 0 && pensum.approvedCredits >= pensum.totalCredits

	return when {
		isComplete -> SummaryStatisticsPaceItem.Projection.Complete(
			text = UiText.Resource(Res.string.summary_statistics_pace_complete)
		)

		remainingTerms != null -> SummaryStatisticsPaceItem.Projection.Remaining(
			text = countText(
				quantity = remainingTerms,
				one = Res.string.summary_statistics_pace_projection_one,
				many = Res.string.summary_statistics_pace_projection_many,
				args = listOf(remainingTerms, pensum.totalCredits)
			),
			emphasis = countText(
				quantity = remainingTerms,
				one = Res.string.summary_statistics_pace_projection_emphasis_one,
				many = Res.string.summary_statistics_pace_projection_emphasis_many,
				args = listOf(remainingTerms)
			),
			tag = UiText.Resource(Res.string.summary_statistics_pace_projection_tag)
		)

		regularTerms < MIN_REGULAR_TERMS_FOR_PROJECTION -> SummaryStatisticsPaceItem.Projection.Pending(
			text = UiText.Resource(Res.string.summary_statistics_pace_projection_pending)
		)

		else -> SummaryStatisticsPaceItem.Projection.None
	}
}
