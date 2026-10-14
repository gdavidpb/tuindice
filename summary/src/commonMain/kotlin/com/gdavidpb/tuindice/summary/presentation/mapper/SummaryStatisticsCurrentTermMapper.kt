package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SUBJECT_POINTS_TOTAL
import com.gdavidpb.tuindice.summary.domain.model.SummaryAttention
import com.gdavidpb.tuindice.summary.domain.model.SummaryCurrentTermStatistics
import com.gdavidpb.tuindice.summary.domain.model.SummaryEvaluationTypeStatistic
import com.gdavidpb.tuindice.summary.domain.model.SummaryHub
import com.gdavidpb.tuindice.summary.domain.model.SummarySubjectPoints
import com.gdavidpb.tuindice.summary.domain.model.SummaryTerm
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCurrentTermItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsEvaluationTypeItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsTooltipItem
import org.jetbrains.compose.resources.StringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_career_percent
import tuindice.summary.generated.resources.summary_points_none
import tuindice.summary.generated.resources.summary_statistics_current_figure
import tuindice.summary.generated.resources.summary_statistics_current_graded_many
import tuindice.summary.generated.resources.summary_statistics_current_graded_one
import tuindice.summary.generated.resources.summary_statistics_current_no_evaluations
import tuindice.summary.generated.resources.summary_statistics_current_none_message
import tuindice.summary.generated.resources.summary_statistics_current_none_title
import tuindice.summary.generated.resources.summary_statistics_current_not_synced_action
import tuindice.summary.generated.resources.summary_statistics_current_not_synced_message
import tuindice.summary.generated.resources.summary_statistics_current_not_synced_title
import tuindice.summary.generated.resources.summary_statistics_current_overdue_many
import tuindice.summary.generated.resources.summary_statistics_current_overdue_one
import tuindice.summary.generated.resources.summary_statistics_current_score
import tuindice.summary.generated.resources.summary_statistics_current_title
import tuindice.summary.generated.resources.summary_statistics_type_attendance
import tuindice.summary.generated.resources.summary_statistics_type_essay
import tuindice.summary.generated.resources.summary_statistics_type_interventions
import tuindice.summary.generated.resources.summary_statistics_type_laboratory
import tuindice.summary.generated.resources.summary_statistics_type_model
import tuindice.summary.generated.resources.summary_statistics_type_other
import tuindice.summary.generated.resources.summary_statistics_type_presentation
import tuindice.summary.generated.resources.summary_statistics_type_project
import tuindice.summary.generated.resources.summary_statistics_type_quiz
import tuindice.summary.generated.resources.summary_statistics_type_report
import tuindice.summary.generated.resources.summary_statistics_type_test
import tuindice.summary.generated.resources.summary_statistics_type_tooltip_attendance
import tuindice.summary.generated.resources.summary_statistics_type_tooltip_essay
import tuindice.summary.generated.resources.summary_statistics_type_tooltip_interventions
import tuindice.summary.generated.resources.summary_statistics_type_tooltip_laboratory
import tuindice.summary.generated.resources.summary_statistics_type_tooltip_model
import tuindice.summary.generated.resources.summary_statistics_type_tooltip_other
import tuindice.summary.generated.resources.summary_statistics_type_tooltip_presentation
import tuindice.summary.generated.resources.summary_statistics_type_tooltip_project
import tuindice.summary.generated.resources.summary_statistics_type_tooltip_quiz
import tuindice.summary.generated.resources.summary_statistics_type_tooltip_report
import tuindice.summary.generated.resources.summary_statistics_type_tooltip_test
import tuindice.summary.generated.resources.summary_statistics_type_tooltip_workshop
import tuindice.summary.generated.resources.summary_statistics_type_tooltip_written_work
import tuindice.summary.generated.resources.summary_statistics_type_workshop
import tuindice.summary.generated.resources.summary_statistics_type_written_work
import kotlin.math.roundToInt

private const val PERCENT = 100

// "Este trimestre": the term in progress as its evaluations tell it. Its name leads to Record. With the evaluations
// downloaded, the block shows the points evaluated out of the total of its subjects and one row per evaluation type;
// until then it says what it lacks.
internal fun currentTermItemOf(
	hub: SummaryHub,
	current: SummaryCurrentTermStatistics?
): SummaryStatisticsCurrentTermItem {
	val title = UiText.Resource(Res.string.summary_statistics_current_title)

	if (current == null) {
		return SummaryStatisticsCurrentTermItem(
			title = title,
			termLink = null,
			termTarget = null,
			content = SummaryStatisticsCurrentTermItem.Content.NoTerm(
				title = UiText.Resource(Res.string.summary_statistics_current_none_title),
				message = UiText.Resource(Res.string.summary_statistics_current_none_message)
			)
		)
	}

	return SummaryStatisticsCurrentTermItem(
		title = title,
		termLink = UiText.Raw("${current.periodCode.shortLabel} ${current.periodYear}"),
		termTarget = SummaryHubTarget.Record,
		content = contentOf(hub = hub, current = current)
	)
}

private fun contentOf(
	hub: SummaryHub,
	current: SummaryCurrentTermStatistics
): SummaryStatisticsCurrentTermItem.Content {
	return when {
		!current.hasSyncedEvaluations -> SummaryStatisticsCurrentTermItem.Content.NotSynced(
			title = UiText.Resource(Res.string.summary_statistics_current_not_synced_title),
			message = UiText.Resource(Res.string.summary_statistics_current_not_synced_message),
			action = UiText.Resource(Res.string.summary_statistics_current_not_synced_action),
			target = SummaryHubTarget.Evaluations
		)

		current.evaluationTypes.isEmpty() -> SummaryStatisticsCurrentTermItem.Content.NoEvaluations(
			text = UiText.Resource(Res.string.summary_statistics_current_no_evaluations)
		)

		else -> SummaryStatisticsCurrentTermItem.Content.Data(
			figure = figureOf(term = hub.term, fraction = current.evaluatedFraction),
			fraction = current.evaluatedFraction?.toFloat(),
			rows = current.evaluationTypes.map { statistic -> rowOf(statistic) },
			overdue = overdueOf(count = current.overdueWithoutGrade, attention = hub.attention)
		)
	}
}

// The points the subjects of the term have evaluated, out of the total they could have: the figure "10 de 400 puntos
// evaluados" and its share. Nothing when the term has no subject to be out of.
private fun figureOf(term: SummaryTerm?, fraction: Double?): SummaryStatisticsFigureItem? {
	val subjects = (term as? SummaryTerm.InProgress)?.subjects.orEmpty()

	if (subjects.isEmpty() || fraction == null) return null

	val evaluated = subjects.sumOf(SummarySubjectPoints::evaluatedOfTotal)
	val total = (SUBJECT_POINTS_TOTAL * subjects.size).roundToInt()

	return SummaryStatisticsFigureItem(
		value = evaluated.formatPoints(),
		label = UiText.Resource(Res.string.summary_statistics_current_figure, listOf(total)),
		trailing = UiText.Resource(
			Res.string.summary_career_percent,
			listOf((fraction * PERCENT).roundToInt())
		)
	)
}

// One row: the type with its graded evaluations out of all of them, and its performance, weighted by the points of each
// graded evaluation. A type with no grade has nothing to point at, so it has no tooltip.
private fun rowOf(statistic: SummaryEvaluationTypeStatistic): SummaryStatisticsEvaluationTypeItem {
	val gradedText = countText(
		quantity = statistic.totalCount,
		one = Res.string.summary_statistics_current_graded_one,
		many = Res.string.summary_statistics_current_graded_many,
		args = listOf(statistic.gradedCount, statistic.totalCount)
	)
	val performance = statistic.performance?.takeIf { statistic.gradedCount > 0 }
	val earned = statistic.earned.formatPoints()
	val evaluated = statistic.evaluated.formatPoints()

	return SummaryStatisticsEvaluationTypeItem(
		name = UiText.Resource(nameOf(statistic.type)),
		gradedText = gradedText,
		scoreText = if (performance == null) {
			UiText.Resource(Res.string.summary_points_none)
		} else {
			UiText.Resource(
				Res.string.summary_statistics_current_score,
				listOf(earned, evaluated, shareOf(performance))
			)
		},
		fraction = performance?.toFloat() ?: 0f,
		tooltip = performance?.let {
			SummaryStatisticsTooltipItem(
				title = UiText.Resource(
					tooltipOf(statistic.type),
					listOf(earned, evaluated, shareOf(performance))
				),
				lines = listOf(gradedText)
			)
		}
	)
}

// The evaluations that went by without a grade, counted, and the week of the oldest as the hub's own line leads there.
// The target is null when the hub has no such line, which happens only while it has none to show.
private fun overdueOf(count: Int, attention: List<SummaryAttention>): SummaryStatisticsCurrentTermItem.Overdue? {
	if (count == 0) return null

	val oldest = attention
		.filterIsInstance<SummaryAttention.UngradedEvaluations>()
		.firstOrNull()

	return SummaryStatisticsCurrentTermItem.Overdue(
		text = countText(
			quantity = count,
			one = Res.string.summary_statistics_current_overdue_one,
			many = Res.string.summary_statistics_current_overdue_many,
			args = listOf(count)
		),
		target = oldest?.let { ungraded ->
			SummaryHubTarget.EvaluationsWeek(
				weekStart = ungraded.oldestWeekStart,
				evaluationId = ungraded.oldestEvaluationId
			)
		}
	)
}

private fun nameOf(type: EvaluationType): StringResource {
	return when (type) {
		EvaluationType.TEST -> Res.string.summary_statistics_type_test
		EvaluationType.ESSAY -> Res.string.summary_statistics_type_essay
		EvaluationType.ATTENDANCE -> Res.string.summary_statistics_type_attendance
		EvaluationType.INTERVENTIONS -> Res.string.summary_statistics_type_interventions
		EvaluationType.LABORATORY -> Res.string.summary_statistics_type_laboratory
		EvaluationType.MODEL -> Res.string.summary_statistics_type_model
		EvaluationType.PRESENTATION -> Res.string.summary_statistics_type_presentation
		EvaluationType.PROJECT -> Res.string.summary_statistics_type_project
		EvaluationType.QUIZ -> Res.string.summary_statistics_type_quiz
		EvaluationType.REPORT -> Res.string.summary_statistics_type_report
		EvaluationType.WORKSHOP -> Res.string.summary_statistics_type_workshop
		EvaluationType.WRITTEN_WORK -> Res.string.summary_statistics_type_written_work
		EvaluationType.OTHER -> Res.string.summary_statistics_type_other
	}
}

// "Quices: %1$s de %2$s (%3$s)", the title of the type's tooltip, with its points and its share.
private fun tooltipOf(type: EvaluationType): StringResource {
	return when (type) {
		EvaluationType.TEST -> Res.string.summary_statistics_type_tooltip_test
		EvaluationType.ESSAY -> Res.string.summary_statistics_type_tooltip_essay
		EvaluationType.ATTENDANCE -> Res.string.summary_statistics_type_tooltip_attendance
		EvaluationType.INTERVENTIONS -> Res.string.summary_statistics_type_tooltip_interventions
		EvaluationType.LABORATORY -> Res.string.summary_statistics_type_tooltip_laboratory
		EvaluationType.MODEL -> Res.string.summary_statistics_type_tooltip_model
		EvaluationType.PRESENTATION -> Res.string.summary_statistics_type_tooltip_presentation
		EvaluationType.PROJECT -> Res.string.summary_statistics_type_tooltip_project
		EvaluationType.QUIZ -> Res.string.summary_statistics_type_tooltip_quiz
		EvaluationType.REPORT -> Res.string.summary_statistics_type_tooltip_report
		EvaluationType.WORKSHOP -> Res.string.summary_statistics_type_tooltip_workshop
		EvaluationType.WRITTEN_WORK -> Res.string.summary_statistics_type_tooltip_written_work
		EvaluationType.OTHER -> Res.string.summary_statistics_type_tooltip_other
	}
}
