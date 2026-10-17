package com.gdavidpb.tuindice.assistant.ui.component.plan

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRecordLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.model.bandText
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.chatcontract.component.PlanComparisonComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_credits
import tuindice.assistant.generated.resources.assistant_card_b_joined
import tuindice.assistant.generated.resources.assistant_card_b_plan_comparison_label
import tuindice.assistant.generated.resources.assistant_card_b_plan_unlocks

// plan.comparacion/1: no figure. One block per candidate plan: its label, its load and its credits, and the subjects it
// unlocks, in the order the service gives them.
@Composable
fun PlanComparisonComponentView(
	component: PlanComparisonComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Plan.comparison(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_plan_comparison_label))
		component.options.forEachIndexed { index, option ->
			val credits = stringResource(Res.string.assistant_card_b_credits, option.credits)
			val band = bandText(option.loadBand)?.asString()
			val summary = if (band == null) credits else stringResource(Res.string.assistant_card_b_joined, band, credits)

			CardRowView(
				code = option.label,
				name = summary,
				showsDivider = index > 0
			)
			if (option.unlocks.isNotEmpty()) {
				CardRowView(
					name = stringResource(Res.string.assistant_card_b_plan_unlocks, option.unlocks.joinToString(", ")),
					showsDivider = false
				)
			}
		}
		CardRecordLinkView(entityRefs = component.entityRefs, onAction = onAction)
	}
}
