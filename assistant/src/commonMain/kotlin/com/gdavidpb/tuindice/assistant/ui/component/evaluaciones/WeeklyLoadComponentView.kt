package com.gdavidpb.tuindice.assistant.ui.component.evaluaciones

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.WeeklyLoadItem
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
import tuindice.assistant.generated.resources.assistant_card_b_load_label

// evaluaciones.carga/1: how many weeks are heavy, then a row per week: its number, its evaluations and its points, with
// the chip of a heavy week. The card links to the record it names, if it names one.
@Composable
fun WeeklyLoadComponentView(
	item: WeeklyLoadItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Evaluations.load(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_load_label))
		CardFiguresView(primary = item.heavyFigure.toCardFigure())
		item.weeks.forEachIndexed { index, week ->
			CardRowView(
				name = week.weekText.asString(),
				showsDivider = index > 0,
				trailing = {
					CardTrailingView(value = week.detailText.asString(), chips = listOfNotNull(week.chip))
				}
			)
		}
		item.recordLink?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
	}
}
