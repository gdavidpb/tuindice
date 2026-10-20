package com.gdavidpb.tuindice.assistant.ui.component.sistema

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.StillAppliesItem
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTitleView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantStillAppliesButtonsView
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_still_applies_title

// sistema.sigue_aplicando/1: a remembered statement and the two answers to it.
@Composable
fun StillAppliesComponentView(
	item: StillAppliesItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier) {
		CardTitleView(text = stringResource(Res.string.assistant_still_applies_title))
		CardBodyTextView(text = item.statement)
		AssistantStillAppliesButtonsView(
			memoryId = item.memoryId,
			onAnswer = { applies ->
				onAction(AssistantChat.Action.AnswerStillApplies(item.memoryId, applies))
			}
		)
	}
}
