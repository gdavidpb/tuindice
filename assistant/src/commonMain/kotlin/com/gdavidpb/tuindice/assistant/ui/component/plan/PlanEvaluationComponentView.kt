package com.gdavidpb.tuindice.assistant.ui.component.plan

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.PlanEvaluationItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardSectionLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_plan_credits
import tuindice.assistant.generated.resources.assistant_card_b_plan_pending_empty
import tuindice.assistant.generated.resources.assistant_card_b_plan_pending_requirements
import tuindice.assistant.generated.resources.assistant_card_b_plan_warnings

// plan.evaluacion/1: the load band of the plan is the figure, with its load index beside it when the service brings
// one. Then the credits, the requirements still pending and the warnings of the slots. Clashes that the service counts
// as zero read as not reviewed yet, because it does not compute them for the subjects the student is not enrolled in.
// With no requirement pending, a line says so; the warnings of the slots appear only when there are some.
@Composable
fun PlanEvaluationComponentView(
	item: PlanEvaluationItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Plan.evaluation(item.id))) {
		CardLabelView(text = item.termLabel)
		item.primaryFigure?.let { primary ->
			CardFiguresView(primary = primary.toCardFigure(), secondary = item.secondaryFigure?.toCardFigure())
		}
		CardRowView(
			name = stringResource(Res.string.assistant_card_b_plan_credits),
			showsDivider = true,
			trailing = { CardValueView(text = item.creditsText.asString()) }
		)
		CardSectionLabelView(text = stringResource(Res.string.assistant_card_b_plan_pending_requirements))
		if (item.pendingRequirements.isEmpty()) {
			CardBodyTextView(text = stringResource(Res.string.assistant_card_b_plan_pending_empty), muted = true)
		}
		item.pendingRequirements.forEachIndexed { index, requirement ->
			CardRowView(name = requirement, showsDivider = index > 0)
		}
		if (item.slotWarnings.isNotEmpty()) {
			CardSectionLabelView(text = stringResource(Res.string.assistant_card_b_plan_warnings))
			item.slotWarnings.forEachIndexed { index, warning ->
				CardRowView(name = warning, showsDivider = index > 0)
			}
		}
		val clashes = item.clashesValueText
		CardRowView(
			name = item.clashesName.asString(),
			showsDivider = true,
			trailing = { clashes?.let { value -> CardValueView(text = value) } }
		)
		item.recordLink?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
	}
}
