package com.gdavidpb.tuindice.assistant.ui.component.plan

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.PlanComparisonItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_plan_comparison_empty
import tuindice.assistant.generated.resources.assistant_card_b_plan_comparison_label

// plan.comparacion/1: no figure. One block per candidate plan: its label as the title, its load and its credits, and
// the subjects it unlocks, in the order the service gives them. With no plan to compare, a line says so.
@Composable
fun PlanComparisonComponentView(
	item: PlanComparisonItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Plan.comparison(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_plan_comparison_label))
		if (item.options.isEmpty()) {
			CardBodyTextView(text = stringResource(Res.string.assistant_card_b_plan_comparison_empty), muted = true)
		}
		item.options.forEachIndexed { index, option ->
			CardRowView(
				title = option.label,
				name = option.summaryText.asString(),
				showsDivider = index > 0
			)
			option.unlocksText?.let { unlocks ->
				CardRowView(name = unlocks.asString(), showsDivider = false)
			}
		}
		item.recordLink?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
	}
}
