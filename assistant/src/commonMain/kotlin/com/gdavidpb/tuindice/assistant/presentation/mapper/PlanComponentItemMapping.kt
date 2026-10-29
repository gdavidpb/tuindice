package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardFigureItem
import com.gdavidpb.tuindice.assistant.presentation.model.PlanComparisonItem
import com.gdavidpb.tuindice.assistant.presentation.model.PlanEvaluationItem
import com.gdavidpb.tuindice.assistant.presentation.model.PlanListItem
import com.gdavidpb.tuindice.assistant.presentation.model.PlanOptionItem
import com.gdavidpb.tuindice.assistant.presentation.model.PlanRowItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.PlanComparisonComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanEvaluationComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanListComponent
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_count_subject_many
import tuindice.assistant.generated.resources.assistant_card_b_count_subject_one
import tuindice.assistant.generated.resources.assistant_card_b_credits
import tuindice.assistant.generated.resources.assistant_card_b_joined
import tuindice.assistant.generated.resources.assistant_card_b_plan_band_caption
import tuindice.assistant.generated.resources.assistant_card_b_plan_band_description
import tuindice.assistant.generated.resources.assistant_card_b_plan_clashes
import tuindice.assistant.generated.resources.assistant_card_b_plan_clashes_unchecked
import tuindice.assistant.generated.resources.assistant_card_b_plan_index_caption
import tuindice.assistant.generated.resources.assistant_card_b_plan_index_description
import tuindice.assistant.generated.resources.assistant_card_b_plan_unlocks
import tuindice.assistant.generated.resources.assistant_card_b_see_all_plans

internal fun PlanListComponent.toPlanListItem(): PlanListItem {
	return PlanListItem(
		id = id,
		opensSheet = opensSheet(),
		rows = plans.map { plan ->
			val subjects = chatCardCountText(
				count = plan.subjectCodes.size,
				one = Res.string.assistant_card_b_count_subject_one,
				many = Res.string.assistant_card_b_count_subject_many
			)

			PlanRowItem(
				termLabel = plan.termLabel,
				summaryText = UiText.Resource(
					Res.string.assistant_card_b_joined,
					listOf(subjects, UiText.Resource(Res.string.assistant_card_b_credits, listOf(plan.credits)))
				),
				link = chatRecordLinkItem(
					entityRefs = listOf(EntityRef(EntityKind.TERM, plan.termKey)),
					termLabel = plan.termLabel
				)
			)
		},
		recordLink = entityRefs.toRecordLinkItem(),
		seeAllText = UiText.Resource(Res.string.assistant_card_b_see_all_plans, listOf(plans.size))
	)
}

internal fun PlanEvaluationComponent.toPlanEvaluationItem(): PlanEvaluationItem {
	val band = planLoadBandText(loadBand)?.let { text ->
		val value = UiText.Raw(text)

		CardFigureItem(
			value = value,
			caption = UiText.Resource(Res.string.assistant_card_b_plan_band_caption),
			contentDescription = UiText.Resource(Res.string.assistant_card_b_plan_band_description, listOf(value))
		)
	}
	val index = loadIndex?.toCardIndexFigureItem(
		caption = Res.string.assistant_card_b_plan_index_caption,
		description = Res.string.assistant_card_b_plan_index_description
	)

	return PlanEvaluationItem(
		id = id,
		opensSheet = opensSheet(),
		termLabel = termLabel,
		primaryFigure = band ?: index,
		secondaryFigure = if (band != null) index else null,
		creditsText = UiText.Resource(Res.string.assistant_card_b_credits, listOf(credits)),
		pendingRequirements = pendingRequirements,
		slotWarnings = slotWarnings,
		clashesName = if (clashes == 0) {
			UiText.Resource(Res.string.assistant_card_b_plan_clashes_unchecked)
		} else {
			UiText.Resource(Res.string.assistant_card_b_plan_clashes)
		},
		clashesValueText = if (clashes == 0) null else clashes.toString(),
		recordLink = entityRefs.toRecordLinkItem()
	)
}

internal fun PlanComparisonComponent.toPlanComparisonItem(): PlanComparisonItem {
	return PlanComparisonItem(
		id = id,
		opensSheet = opensSheet(),
		options = options.map { option ->
			val credits = UiText.Resource(Res.string.assistant_card_b_credits, listOf(option.credits))
			val band = planLoadBandText(option.loadBand)

			PlanOptionItem(
				label = option.label,
				summaryText = if (band == null) {
					credits
				} else {
					UiText.Resource(Res.string.assistant_card_b_joined, listOf(band, credits))
				},
				unlocksText = option.unlocks.takeIf { unlocks -> unlocks.isNotEmpty() }?.let { unlocks ->
					UiText.Resource(Res.string.assistant_card_b_plan_unlocks, listOf(unlocks.joinToString(", ")))
				}
			)
		},
		recordLink = entityRefs.toRecordLinkItem()
	)
}
