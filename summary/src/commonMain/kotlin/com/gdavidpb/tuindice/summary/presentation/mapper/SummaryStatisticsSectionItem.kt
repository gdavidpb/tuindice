package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.TermStatistic
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummaryHub
import com.gdavidpb.tuindice.summary.domain.model.SummaryStatistics
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsIndexItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSectionItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_statistics_section_approved_best_many
import tuindice.summary.generated.resources.summary_statistics_section_approved_best_one
import tuindice.summary.generated.resources.summary_statistics_subjects_approved_many
import tuindice.summary.generated.resources.summary_statistics_subjects_approved_one

/**
 * What the section of the hub says of the statistics, or null when the record is not ready and the hub has none. Terms
 * over make the figures. Without one, a term in progress invites to read the statistics once it is over; with neither,
 * the section says what it will show and has no link. The figures are the ones the Statistics screen reads from
 * the same terms, so the two agree.
 */
fun SummaryHub.toSummaryStatisticsSectionItem(): SummaryStatisticsSectionItem? {
	val summary = statistics ?: return null

	return when {
		summary.closed.terms.isNotEmpty() -> figuresOf(summary)
		summary.currentTerm != null -> SummaryStatisticsSectionItem.Invite
		else -> SummaryStatisticsSectionItem.Empty
	}
}

// The cumulative average at the last term that has one, and the line of those averages, as the screen draws them.
// A term over with no average has neither, and its phrase counts the subjects approved alone.
private fun figuresOf(summary: SummaryStatistics): SummaryStatisticsSectionItem.Figures {
	val phrase = phraseOf(approved = summary.closed.subjects.approved, best = summary.bestTerm)
	val content = indexItemOf(summary = summary, axisLabels = axisLabelsOf(summary.closed.terms)).content

	return when (content) {
		is SummaryStatisticsIndexItem.Content.Data -> SummaryStatisticsSectionItem.Figures(
			grade = content.grade,
			line = content.points.map { point -> point.lineValue },
			phrase = phrase
		)

		is SummaryStatisticsIndexItem.Content.Empty -> SummaryStatisticsSectionItem.Figures(
			grade = null,
			line = emptyList(),
			phrase = phrase
		)
	}
}

// "44 aprobadas · mejor trimestre Ene - Mar 2024", or "44 aprobadas" without a regular term that has a best one. The
// singular is for one subject, as the subjects block writes it.
private fun phraseOf(approved: Int, best: TermStatistic?): UiText {
	return if (best == null) {
		countText(
			quantity = approved,
			one = Res.string.summary_statistics_subjects_approved_one,
			many = Res.string.summary_statistics_subjects_approved_many,
			args = listOf(approved)
		)
	} else {
		countText(
			quantity = approved,
			one = Res.string.summary_statistics_section_approved_best_one,
			many = Res.string.summary_statistics_section_approved_best_many,
			args = listOf(approved, best.fullLabel())
		)
	}
}
