package com.gdavidpb.tuindice.assistant.ui.component.evaluaciones

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationListItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTrailingView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_evaluations_label

// evaluaciones.lista/1: how many evaluations there are, then a row per evaluation: its subject and title, its date, its
// grade over the maximum and the chip of its state. The card links to the record it names, if it names one.
@Composable
fun EvaluationListComponentView(
	item: EvaluationListItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Evaluations.list(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_evaluations_label))
		CardFiguresView(primary = item.countFigure.toCardFigure())
		item.rows.forEachIndexed { index, row ->
			CardRowView(
				code = row.code,
				name = row.nameText.asString(),
				showsDivider = index > 0,
				trailing = {
					CardTrailingView(value = row.gradeText.asString(), chips = listOfNotNull(row.stateChip))
				}
			)
		}
		item.recordLink?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
	}
}
