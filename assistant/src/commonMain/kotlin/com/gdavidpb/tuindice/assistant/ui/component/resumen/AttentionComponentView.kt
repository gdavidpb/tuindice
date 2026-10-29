package com.gdavidpb.tuindice.assistant.ui.component.resumen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.AttentionCardItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowText
import com.gdavidpb.tuindice.assistant.ui.model.visibleRows
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_attention_empty
import tuindice.assistant.generated.resources.assistant_card_a_attention_label

// resumen.atencion/1: one row per item that needs attention. An item links to the screen of the record it names, and a
// tap anywhere on its row opens that screen; a kind of record with no screen of its own has no link. With nothing to
// attend, a line says so.
@Composable
fun AttentionComponentView(
	item: AttentionCardItem,
	rowLimit: Int?,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Summary.attention(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_attention_label))
		if (item.rows.isEmpty()) {
			CardBodyTextView(text = stringResource(Res.string.assistant_card_a_attention_empty), muted = true)
		}
		item.rows.visibleRows(rowLimit).forEachIndexed { index, row ->
			CardLinkRowView(
				link = row.link,
				onAction = onAction,
				text = CardRowText(name = row.detail, title = row.title),
				showsDivider = index > 0
			)
		}
	}
}
