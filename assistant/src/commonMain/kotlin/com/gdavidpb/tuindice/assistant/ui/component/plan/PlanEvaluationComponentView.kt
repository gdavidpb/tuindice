package com.gdavidpb.tuindice.assistant.ui.component.plan

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.planLoadBandText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRecordLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.CardFigure
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.model.toCardIndexFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.chatcontract.component.PlanEvaluationComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_credits
import tuindice.assistant.generated.resources.assistant_card_b_plan_band_caption
import tuindice.assistant.generated.resources.assistant_card_b_plan_band_description
import tuindice.assistant.generated.resources.assistant_card_b_plan_clashes
import tuindice.assistant.generated.resources.assistant_card_b_plan_clashes_unchecked
import tuindice.assistant.generated.resources.assistant_card_b_plan_credits_label
import tuindice.assistant.generated.resources.assistant_card_b_plan_index_caption
import tuindice.assistant.generated.resources.assistant_card_b_plan_index_description
import tuindice.assistant.generated.resources.assistant_card_b_plan_pending_requirements
import tuindice.assistant.generated.resources.assistant_card_b_plan_warnings

// plan.evaluacion/1: the load band of the plan is the figure, with its load index beside it when the service brings
// one. Then the credits, the requirements still pending and the warnings of the slots. Clashes that the service counts
// as zero read as not reviewed yet, because it does not compute them for the subjects the student is not enrolled in.
@Composable
fun PlanEvaluationComponentView(
	component: PlanEvaluationComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val bandFigure = planLoadBandText(component.loadBand)?.let { band ->
		CardFigure(
			value = band,
			caption = stringResource(Res.string.assistant_card_b_plan_band_caption),
			contentDescription = stringResource(Res.string.assistant_card_b_plan_band_description, band)
		)
	}
	val indexFigure = component.loadIndex?.toCardIndexFigure(
		caption = Res.string.assistant_card_b_plan_index_caption,
		description = Res.string.assistant_card_b_plan_index_description
	)

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Plan.evaluation(component.id))) {
		CardLabelView(text = component.termLabel)
		(bandFigure ?: indexFigure)?.let { primary ->
			CardFiguresView(primary = primary, secondary = if (bandFigure != null) indexFigure else null)
		}
		CardRowView(
			name = stringResource(Res.string.assistant_card_b_plan_credits_label),
			trailing = { CardValueView(text = stringResource(Res.string.assistant_card_b_credits, component.credits)) }
		)
		if (component.pendingRequirements.isNotEmpty()) {
			CardLabelView(text = stringResource(Res.string.assistant_card_b_plan_pending_requirements))
			component.pendingRequirements.forEachIndexed { index, requirement ->
				CardRowView(name = requirement, showsDivider = index > 0)
			}
		}
		if (component.slotWarnings.isNotEmpty()) {
			CardLabelView(text = stringResource(Res.string.assistant_card_b_plan_warnings))
			component.slotWarnings.forEachIndexed { index, warning ->
				CardRowView(name = warning, showsDivider = index > 0)
			}
		}
		if (component.clashes == 0) {
			CardRowView(name = stringResource(Res.string.assistant_card_b_plan_clashes_unchecked))
		} else {
			CardRowView(
				name = stringResource(Res.string.assistant_card_b_plan_clashes),
				trailing = { CardValueView(text = component.clashes.toString()) }
			)
		}
		CardRecordLinkView(entityRefs = component.entityRefs, onAction = onAction)
	}
}
