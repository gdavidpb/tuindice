package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardChipTone
import com.gdavidpb.tuindice.assistant.presentation.model.CardFigureItem
import com.gdavidpb.tuindice.assistant.presentation.model.CountRowItem
import com.gdavidpb.tuindice.assistant.presentation.model.CreditsRowItem
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
import com.gdavidpb.tuindice.chatcontract.component.IndexPoint
import com.gdavidpb.tuindice.chatcontract.component.IndexSeriesComponent
import com.gdavidpb.tuindice.chatcontract.component.PaceComponent
import com.gdavidpb.tuindice.chatcontract.component.RecordStatsComponent
import com.gdavidpb.tuindice.chatcontract.component.SimulationComponent
import com.gdavidpb.tuindice.chatcontract.component.TermComponent
import com.gdavidpb.tuindice.chatcontract.component.TermSubjectRow
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_cumulative_caption
import tuindice.assistant.generated.resources.assistant_card_a_cumulative_description
import tuindice.assistant.generated.resources.assistant_card_a_goal_caption
import tuindice.assistant.generated.resources.assistant_card_a_goal_description
import tuindice.assistant.generated.resources.assistant_card_a_goal_feasible
import tuindice.assistant.generated.resources.assistant_card_a_goal_not_feasible
import tuindice.assistant.generated.resources.assistant_card_a_no_value
import tuindice.assistant.generated.resources.assistant_card_a_pace_caption
import tuindice.assistant.generated.resources.assistant_card_a_pace_description
import tuindice.assistant.generated.resources.assistant_card_a_profile_index_caption
import tuindice.assistant.generated.resources.assistant_card_a_profile_index_description
import tuindice.assistant.generated.resources.assistant_card_a_record_approved_caption_many
import tuindice.assistant.generated.resources.assistant_card_a_record_approved_caption_one
import tuindice.assistant.generated.resources.assistant_card_a_record_approved_description
import tuindice.assistant.generated.resources.assistant_card_a_record_failed
import tuindice.assistant.generated.resources.assistant_card_a_record_first_try
import tuindice.assistant.generated.resources.assistant_card_a_record_repeated
import tuindice.assistant.generated.resources.assistant_card_a_record_retired
import tuindice.assistant.generated.resources.assistant_card_a_see_all_terms
import tuindice.assistant.generated.resources.assistant_card_a_series_best
import tuindice.assistant.generated.resources.assistant_card_a_series_no_value
import tuindice.assistant.generated.resources.assistant_card_a_series_row_best_description
import tuindice.assistant.generated.resources.assistant_card_a_series_row_description
import tuindice.assistant.generated.resources.assistant_card_a_series_row_worst_description
import tuindice.assistant.generated.resources.assistant_card_a_series_worst
import tuindice.assistant.generated.resources.assistant_card_a_simulation_cumulative_caption
import tuindice.assistant.generated.resources.assistant_card_a_simulation_cumulative_description
import tuindice.assistant.generated.resources.assistant_card_a_simulation_index_caption
import tuindice.assistant.generated.resources.assistant_card_a_simulation_index_description
import tuindice.assistant.generated.resources.assistant_card_a_term_index_caption
import tuindice.assistant.generated.resources.assistant_card_a_term_index_description
import tuindice.assistant.generated.resources.assistant_card_b_credits

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
		rows = rows.map { row -> row.toTermRowItem() },
		recordLink = if (entityRefs.any { ref -> ref.kind == EntityKind.TERM }) {
			chatRecordLinkItem(entityRefs = entityRefs, termLabel = termLabel)
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
			caption = Res.string.assistant_card_a_profile_index_caption,
			description = Res.string.assistant_card_a_profile_index_description
		),
		points = points.map { point ->
			point.toIndexPointItem(isBest = point.termId == bestTermId, isWorst = point.termId == worstTermId)
		},
		seeAllText = UiText.Resource(Res.string.assistant_card_a_see_all_terms, listOf(points.size))
	)
}

internal fun RecordStatsComponent.toRecordStatsItem(): RecordStatsItem {
	val approvedText = UiText.Raw(approved.toString())

	return RecordStatsItem(
		id = id,
		opensSheet = opensSheet(),
		approvedFigure = CardFigureItem(
			value = approvedText,
			caption = chatCardNounText(
				count = approved,
				one = Res.string.assistant_card_a_record_approved_caption_one,
				many = Res.string.assistant_card_a_record_approved_caption_many
			),
			contentDescription = UiText.Resource(Res.string.assistant_card_a_record_approved_description, listOf(approvedText))
		),
		counts = listOf(
			Res.string.assistant_card_a_record_failed to failed,
			Res.string.assistant_card_a_record_retired to retired,
			Res.string.assistant_card_a_record_first_try to approvedFirstTry,
			Res.string.assistant_card_a_record_repeated to repeated
		).map { (label, count) -> CountRowItem(label = UiText.Resource(label), value = count.toString()) },
		histogram = histogram.toHistogramBarItems()
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
		terms = creditsPerTerm.map { term ->
			CreditsRowItem(
				label = term.termLabel,
				credits = UiText.Resource(Res.string.assistant_card_b_credits, listOf(term.credits))
			)
		},
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
				outcomeText = attemptOutcomeChip(change.outcome)?.text ?: UiText.Empty,
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
		feasibilityChip = if (feasible) {
			chatCardChip(Res.string.assistant_card_a_goal_feasible, CardChipTone.Success)
		} else {
			chatCardChip(Res.string.assistant_card_a_goal_not_feasible, CardChipTone.Failed)
		},
		targetText = targetCumulative.toCardIndexText(),
		creditsText = UiText.Resource(Res.string.assistant_card_b_credits, listOf(credits))
	)
}

private const val AVERAGE_DECIMALS = 1

// A subject of a term: its grade, or when it has none, the chip of its result. A subject with neither reads a dash, so
// its row never ends in an empty space.
private fun TermSubjectRow.toTermRowItem(): TermRowItem {
	val outcomeChip = if (grade == null) attemptOutcomeChip(outcome) else null

	return TermRowItem(
		code = subjectCode,
		name = subjectName,
		valueText = when {
			grade != null -> UiText.Raw(grade.toString())
			outcomeChip == null -> UiText.Resource(Res.string.assistant_card_a_no_value)
			else -> null
		},
		outcomeChip = outcomeChip,
		badgeChip = attemptBadgeChip(badge)
	)
}

// One term of the series: its index and its cumulative index, the sentence a screen reader says for it, and the chip of
// the best or the worst term. A missing index reads as no data in that sentence.
private fun IndexPoint.toIndexPointItem(isBest: Boolean, isWorst: Boolean): IndexPointItem {
	val noValue = UiText.Resource(Res.string.assistant_card_a_series_no_value)
	val indexText = termIndex?.toCardIndexText()
	val cumulativeText = cumulativeIndex?.toCardIndexText()
	val descriptionText = when {
		isBest -> Res.string.assistant_card_a_series_row_best_description
		isWorst -> Res.string.assistant_card_a_series_row_worst_description
		else -> Res.string.assistant_card_a_series_row_description
	}

	return IndexPointItem(
		termLabel = termLabel,
		termIndexText = indexText,
		cumulativeIndexText = cumulativeText,
		description = UiText.Resource(descriptionText, listOf(termLabel, indexText ?: noValue, cumulativeText ?: noValue)),
		markChip = when {
			isBest -> chatCardChip(Res.string.assistant_card_a_series_best, CardChipTone.Neutral)
			isWorst -> chatCardChip(Res.string.assistant_card_a_series_worst, CardChipTone.Neutral)
			else -> null
		}
	)
}
