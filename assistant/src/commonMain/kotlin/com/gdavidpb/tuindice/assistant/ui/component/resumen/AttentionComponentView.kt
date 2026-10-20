package com.gdavidpb.tuindice.assistant.ui.component.resumen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.AttentionCardItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_attention_label

// resumen.atencion/1: how many items need attention, then one row per item. An item links to the screen of the record
// it names; a kind of record with no screen of its own has no link.
@Composable
fun AttentionComponentView(
	item: AttentionCardItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Summary.attention(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_attention_label))
		CardFiguresView(primary = item.count.toCardFigure())
		item.rows.forEachIndexed { index, row ->
			CardRowView(
				title = row.title,
				name = row.detail,
				showsDivider = index > 0,
				trailing = {
					row.link?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
				}
			)
		}
	}
}
