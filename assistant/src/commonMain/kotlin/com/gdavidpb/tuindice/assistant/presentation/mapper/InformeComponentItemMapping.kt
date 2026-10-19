package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardFigureItem
import com.gdavidpb.tuindice.assistant.presentation.model.CardLinkItem
import com.gdavidpb.tuindice.assistant.presentation.model.CountRowItem
import com.gdavidpb.tuindice.assistant.presentation.model.CreditsRowItem
import com.gdavidpb.tuindice.assistant.presentation.model.HistogramBarItem
import com.gdavidpb.tuindice.assistant.presentation.model.IndexGoalItem
import com.gdavidpb.tuindice.assistant.presentation.model.IndexPointItem
import com.gdavidpb.tuindice.assistant.presentation.model.IndexSeriesItem
import com.gdavidpb.tuindice.assistant.presentation.model.PaceItem
import com.gdavidpb.tuindice.assistant.presentation.model.RecordStatsItem
import com.gdavidpb.tuindice.assistant.presentation.model.SimulatedGradeItem
import com.gdavidpb.tuindice.assistant.presentation.model.SimulationItem
import com.gdavidpb.tuindice.assistant.presentation.model.TermItem
import com.gdavidpb.tuindice.assistant.presentation.model.TermRowItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.IndexGoalComponent
import com.gdavidpb.tuindice.chatcontract.component.IndexSeriesComponent
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.PaceComponent
import com.gdavidpb.tuindice.chatcontract.component.RecordStatsComponent
import com.gdavidpb.tuindice.chatcontract.component.SimulationComponent
import com.gdavidpb.tuindice.chatcontract.component.TermComponent
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_cumulative_caption
import tuindice.assistant.generated.resources.assistant_card_a_cumulative_description
import tuindice.assistant.generated.resources.assistant_card_a_goal_caption
import tuindice.assistant.generated.resources.assistant_card_a_goal_description
import tuindice.assistant.generated.resources.assistant_card_a_goal_feasible
import tuindice.assistant.generated.resources.assistant_card_a_goal_not_feasible
import tuindice.assistant.generated.resources.assistant_card_a_link_record
import tuindice.assistant.generated.resources.assistant_card_a_pace_caption
import tuindice.assistant.generated.resources.assistant_card_a_pace_description
import tuindice.assistant.generated.resources.assistant_card_a_record_approved_caption
import tuindice.assistant.generated.resources.assistant_card_a_record_approved_description
import tuindice.assistant.generated.resources.assistant_card_a_record_failed
import tuindice.assistant.generated.resources.assistant_card_a_record_first_try
import tuindice.assistant.generated.resources.assistant_card_a_record_repeated
import tuindice.assistant.generated.resources.assistant_card_a_record_retired
import tuindice.assistant.generated.resources.assistant_card_a_series_best
import tuindice.assistant.generated.resources.assistant_card_a_series_caption
import tuindice.assistant.generated.resources.assistant_card_a_series_description
import tuindice.assistant.generated.resources.assistant_card_a_series_worst
import tuindice.assistant.generated.resources.assistant_card_a_simulation_cumulative_caption
import tuindice.assistant.generated.resources.assistant_card_a_simulation_cumulative_description
import tuindice.assistant.generated.resources.assistant_card_a_simulation_index_caption
import tuindice.assistant.generated.resources.assistant_card_a_simulation_index_description
import tuindice.assistant.generated.resources.assistant_card_a_term_index_caption
import tuindice.assistant.generated.resources.assistant_card_a_term_index_description

// The figure of the term: its index first, and the cumulative index beside it when the term has one too.
private fun primaryAndSecondary(
	termIndex: CardFigureItem?,
	cumulative: CardFigureItem?
): Pair<CardFigureItem?, CardFigureItem?> = (termIndex ?: cumulative) to (if (termIndex != null) cumulative else null)

internal fun TermComponent.toTermItem(): TermItem {
	val indexFigure = termIndex?.toCardIndexFigureItem(
		caption = Res.string.assistant_card_a_term_index_caption,
		description = Res.string.assistant_card_a_term_index_description
	)
	val cumulativeFigure = cumulativeIndex?.toCardIndexFigureItem(
		caption = Res.string.assistant_card_a_cumulative_caption,
		description = Res.string.assistant_card_a_cumulative_description
	)
	val (primary, secondary) = primaryAndSecondary(indexFigure, cumulativeFigure)

	return TermItem(
		id = id,
		opensSheet = opensSheet(),
		termLabel = termLabel,
		primaryFigure = primary,
		secondaryFigure = secondary,
		rows = rows.map { row ->
			TermRowItem(
				code = row.subjectCode,
				name = row.subjectName,
				gradeText = row.grade?.toString(),
				outcomeText = if (row.grade == null) attemptOutcomeText(row.outcome) else null,
				badgeText = attemptBadgeText(row.badge)
			)
		},
		recordLink = if (entityRefs.any { ref -> ref.kind == EntityKind.TERM }) {
			CardLinkItem(
				target = NavTarget.RECORD,
				text = UiText.Resource(Res.string.assistant_card_a_link_record),
				entityRefs = entityRefs
			)
		} else {
			null
		}
	)
}

internal fun IndexSeriesComponent.toIndexSeriesItem(): IndexSeriesItem {
	val current = points.lastOrNull { point -> point.cumulativeIndex != null }?.cumulativeIndex

	return IndexSeriesItem(
		id = id,
		opensSheet = opensSheet(),
		currentFigure = current?.toCardIndexFigureItem(
			caption = Res.string.assistant_card_a_series_caption,
			description = Res.string.assistant_card_a_series_description
		),
		points = points.map { point ->
			IndexPointItem(
				termLabel = point.termLabel,
				termIndexText = point.termIndex?.toCardIndexText(),
				cumulativeIndexText = point.cumulativeIndex?.toCardIndexText(),
				markText = when (point.termId) {
					bestTermId -> UiText.Resource(Res.string.assistant_card_a_series_best)
					worstTermId -> UiText.Resource(Res.string.assistant_card_a_series_worst)
					else -> null
				}
			)
		}
	)
}

internal fun RecordStatsComponent.toRecordStatsItem(): RecordStatsItem {
	val approvedText = UiText.Raw(approved.toString())
	val largest = histogram.maxOrNull() ?: 0

	return RecordStatsItem(
		id = id,
		opensSheet = opensSheet(),
		approvedFigure = CardFigureItem(
			value = approvedText,
			caption = UiText.Resource(Res.string.assistant_card_a_record_approved_caption),
			contentDescription = UiText.Resource(Res.string.assistant_card_a_record_approved_description, listOf(approvedText))
		),
		counts = listOf(
			Res.string.assistant_card_a_record_failed to failed,
			Res.string.assistant_card_a_record_retired to retired,
			Res.string.assistant_card_a_record_first_try to approvedFirstTry,
			Res.string.assistant_card_a_record_repeated to repeated
		).map { (label, count) -> CountRowItem(label = UiText.Resource(label), value = count.toString()) },
		histogram = histogram.mapIndexed { index, count ->
			HistogramBarItem(
				grade = (index + 1).toString(),
				count = count.toString(),
				fraction = if (largest == 0) 0f else count.toFloat() / largest
			)
		}
	)
}

internal fun PaceComponent.toPaceItem(): PaceItem {
	val average = averageCredits.toCardDecimalText(AVERAGE_DECIMALS)

	return PaceItem(
		id = id,
		opensSheet = opensSheet(),
		averageFigure = CardFigureItem(
			value = UiText.Raw(average),
			caption = UiText.Resource(Res.string.assistant_card_a_pace_caption),
			contentDescription = UiText.Resource(Res.string.assistant_card_a_pace_description, listOf(average))
		),
		terms = creditsPerTerm.map { term -> CreditsRowItem(label = term.termLabel, credits = term.credits.toString()) },
		remainingTerms = remainingTermsEstimate?.toString()
	)
}

internal fun SimulationComponent.toSimulationItem(): SimulationItem {
	val indexFigure = termIndex?.toCardIndexFigureItem(
		caption = Res.string.assistant_card_a_simulation_index_caption,
		description = Res.string.assistant_card_a_simulation_index_description
	)
	val cumulativeFigure = cumulativeIndex?.toCardIndexFigureItem(
		caption = Res.string.assistant_card_a_simulation_cumulative_caption,
		description = Res.string.assistant_card_a_simulation_cumulative_description
	)
	val (primary, secondary) = primaryAndSecondary(indexFigure, cumulativeFigure)

	return SimulationItem(
		id = id,
		opensSheet = opensSheet(),
		primaryFigure = primary,
		secondaryFigure = secondary,
		changes = changes.map { change ->
			SimulatedGradeItem(
				code = change.subjectCode,
				outcomeText = attemptOutcomeText(change.outcome) ?: UiText.Empty,
				gradeText = change.grade?.toString()
			)
		}
	)
}

internal fun IndexGoalComponent.toIndexGoalItem(): IndexGoalItem {
	return IndexGoalItem(
		id = id,
		opensSheet = opensSheet(),
		requiredFigure = requiredTermIndex?.toCardIndexFigureItem(
			caption = Res.string.assistant_card_a_goal_caption,
			description = Res.string.assistant_card_a_goal_description
		),
		feasibilityText = UiText.Resource(
			if (feasible) Res.string.assistant_card_a_goal_feasible else Res.string.assistant_card_a_goal_not_feasible
		),
		targetText = targetCumulative.toCardIndexText(),
		creditsText = credits.toString()
	)
}

private const val AVERAGE_DECIMALS = 1
