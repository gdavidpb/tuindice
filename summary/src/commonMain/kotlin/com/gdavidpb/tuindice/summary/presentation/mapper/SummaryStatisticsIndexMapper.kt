package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.TermStatistic
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsIndexItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsLinePointItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsTooltipItem
import org.jetbrains.compose.resources.StringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_statistics_index_best
import tuindice.summary.generated.resources.summary_statistics_index_caption
import tuindice.summary.generated.resources.summary_statistics_index_empty
import tuindice.summary.generated.resources.summary_statistics_index_suffix
import tuindice.summary.generated.resources.summary_statistics_index_title
import tuindice.summary.generated.resources.summary_statistics_index_tooltip_credits_many
import tuindice.summary.generated.resources.summary_statistics_index_tooltip_credits_one
import tuindice.summary.generated.resources.summary_statistics_index_tooltip_index
import tuindice.summary.generated.resources.summary_statistics_index_worst
import com.gdavidpb.tuindice.summary.domain.model.SummaryStatistics as SummaryStatisticsModel

private const val INDEX_DECIMALS = 4
private const val MIN_RANKED_TERMS_FOR_WORST = 2

// "Índice": one point per closed term that has an index, in order. The line runs through the cumulative average, and
// the marker of each point sits on the index of its term. The figure is the cumulative average at the last of them,
// which the caption names. The best regular term is named under the chart, and the worst from two ranked terms on.
internal fun indexItemOf(summary: SummaryStatisticsModel, axisLabels: Map<String, String>): SummaryStatisticsIndexItem {
	val title = UiText.Resource(Res.string.summary_statistics_index_title)
	val indexed = summary.closed.terms.filter { term -> term.index != null }
	val bestKey = summary.bestTerm?.termKey
	val points = indexed.mapNotNull { term ->
		linePointOf(term = term, axisLabels = axisLabels)?.let { point -> point.copy(isBest = term.termKey == bestKey) }
	}
	val last = indexed.lastOrNull()
	val lastPoint = points.lastOrNull()

	if (last == null || lastPoint == null) {
		return SummaryStatisticsIndexItem(
			title = title,
			content = SummaryStatisticsIndexItem.Content.Empty(
				text = UiText.Resource(Res.string.summary_statistics_index_empty)
			)
		)
	}

	val values = points.flatMap { point -> listOf(point.lineValue, point.markerValue) }
	val rankedTerms = summary.closed.terms.count { term -> term.isRegular && term.index != null }

	return SummaryStatisticsIndexItem(
		title = title,
		content = SummaryStatisticsIndexItem.Content.Data(
			figure = SummaryStatisticsFigureItem(
				value = lastPoint.lineValue.toDouble().formatGrade(decimals = INDEX_DECIMALS),
				label = UiText.Resource(Res.string.summary_statistics_index_suffix)
			),
			caption = UiText.Resource(
				Res.string.summary_statistics_index_caption,
				listOf(last.fullLabel())
			),
			points = points,
			minValue = values.min(),
			maxValue = values.max(),
			best = summary.bestTerm?.let { best -> rankedLabelOf(Res.string.summary_statistics_index_best, best) },
			worst = summary.worstTerm
				?.takeIf { rankedTerms >= MIN_RANKED_TERMS_FOR_WORST }
				?.let { worst -> rankedLabelOf(Res.string.summary_statistics_index_worst, worst) }
		)
	)
}

private fun linePointOf(term: TermStatistic, axisLabels: Map<String, String>): SummaryStatisticsLinePointItem? {
	val index = term.index ?: return null
	val cumulative = term.cumulativeIndex ?: index

	return SummaryStatisticsLinePointItem(
		axisLabel = UiText.Raw(axisLabels.getValue(term.termKey)),
		lineValue = cumulative.toFloat(),
		markerValue = index.toFloat(),
		tooltip = SummaryStatisticsTooltipItem(
			title = UiText.Raw(term.fullLabel()),
			lines = listOf(
				UiText.Resource(
					Res.string.summary_statistics_index_tooltip_index,
					listOf(
						index.formatGrade(decimals = INDEX_DECIMALS),
						cumulative.formatGrade(decimals = INDEX_DECIMALS)
					)
				),
				countText(
					quantity = term.approvedCredits,
					one = Res.string.summary_statistics_index_tooltip_credits_one,
					many = Res.string.summary_statistics_index_tooltip_credits_many,
					args = listOf(term.approvedCredits)
				)
			)
		)
	)
}

// "Mejor: Ene - Mar 2024 · 4.5714", the term and its index.
private fun rankedLabelOf(resource: StringResource, term: TermStatistic): UiText {
	val index = term.index ?: 0.0

	return UiText.Resource(
		resource,
		listOf(term.fullLabel(), index.formatGrade(decimals = INDEX_DECIMALS))
	)
}
