package com.gdavidpb.tuindice.assistant.ui.component.pensum

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.PensumProgressItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardProgressView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_progress_label

// pensum.avance/1: the share of the credits approved is the figure, with the credits beside it, and a bar of the same
// share. Then how many subjects are approved, in progress, available and blocked, one row each.
@Composable
fun PensumProgressComponentView(
	item: PensumProgressItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Pensum.progress(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_progress_label))
		CardFiguresView(
			primary = item.primaryFigure.toCardFigure(),
			secondary = item.secondaryFigure.toCardFigure()
		)
		CardProgressView(fraction = item.progressFraction)
		item.statusRows.forEachIndexed { index, row ->
			CardRowView(
				name = row.label.asString(),
				showsDivider = index > 0,
				trailing = { CardValueView(text = row.value) }
			)
		}
		item.recordLink?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
	}
}
