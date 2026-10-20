package com.gdavidpb.tuindice.assistant.ui.component.plan

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.PlanListItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTrailingColumn
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.visibleRows
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_plans_empty
import tuindice.assistant.generated.resources.assistant_card_b_plans_label

// plan.lista/1: a row per term plan: its term, and on the right its subjects and credits with a link to the record of
// that term. With no plan, a line says so.
@Composable
fun PlanListComponentView(
	item: PlanListItem,
	rowLimit: Int?,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Plan.list(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_plans_label))
		if (item.rows.isEmpty()) {
			CardBodyTextView(text = stringResource(Res.string.assistant_card_b_plans_empty), muted = true)
		}
		item.rows.visibleRows(rowLimit).forEachIndexed { index, plan ->
			CardRowView(
				name = plan.termLabel,
				showsDivider = index > 0,
				trailing = {
					CardTrailingColumn {
						CardValueView(text = plan.summaryText.asString())
						CardLinkItemView(link = plan.link, onAction = onAction)
					}
				}
			)
		}
		item.recordLink?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
	}
}
