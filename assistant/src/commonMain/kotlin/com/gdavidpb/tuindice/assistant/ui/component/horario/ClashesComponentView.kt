package com.gdavidpb.tuindice.assistant.ui.component.horario

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.model.ClashesItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.model.visibleRows
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_clashes_empty
import tuindice.assistant.generated.resources.assistant_card_a_clashes_label

// horario.choques/1: one row per clash: the subjects that meet at the same time as its title, and the day and
// the blocks of the clash. With no clash, a line says so.
@Composable
fun ClashesComponentView(
	item: ClashesItem,
	rowLimit: Int?,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Schedule.clashes(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_clashes_label))
		if (item.rows.isEmpty()) {
			CardBodyTextView(text = stringResource(Res.string.assistant_card_a_clashes_empty), muted = true)
		}
		item.rows.visibleRows(rowLimit).forEachIndexed { index, clash ->
			CardRowView(
				codes = clash.codes,
				name = clash.dayAndBlocksText.asString(),
				showsDivider = index > 0
			)
		}
	}
}
