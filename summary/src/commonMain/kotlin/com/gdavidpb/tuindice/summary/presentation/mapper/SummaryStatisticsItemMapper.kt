package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummaryHub
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_statistics_chart_action

/**
 * What the Statistics screen shows of the hub, or null when the hub has no statistics, which is when the record is not
 * ready: the screen is then unavailable. The six blocks are always present, in the order the screen lists them, each
 * with its data or with the text that says what it lacks. The figures are the ones the hub reads, so the screen agrees
 * with the hub on every figure they have in common.
 */
fun SummaryHub.toSummaryStatisticsItem(): SummaryStatisticsItem? {
	val summary = statistics ?: return null
	val axisLabels = axisLabelsOf(summary.closed.terms)

	return SummaryStatisticsItem(
		currentTerm = currentTermItemOf(hub = this, current = summary.currentTerm),
		index = indexItemOf(summary = summary, axisLabels = axisLabels),
		subjects = subjectsItemOf(outcomes = summary.closed.subjects),
		credits = creditsItemOf(outcomes = summary.closed.credits, pensum = summary.pensumProgress),
		grades = gradesItemOf(histogram = summary.closed.grades),
		pace = paceItemOf(summary = summary, axisLabels = axisLabels),
		chartActionLabel = UiText.Resource(Res.string.summary_statistics_chart_action)
	)
}
