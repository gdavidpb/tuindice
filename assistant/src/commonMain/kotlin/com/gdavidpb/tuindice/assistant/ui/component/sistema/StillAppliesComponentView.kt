package com.gdavidpb.tuindice.assistant.ui.component.sistema

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantStillAppliesButtonsView
import com.gdavidpb.tuindice.chatcontract.component.StillAppliesComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_still_applies_title

// sistema.sigue_aplicando/1: a remembered statement and the two answers to it.
@Composable
fun StillAppliesComponentView(
	component: StillAppliesComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier) {
		Text(
			text = stringResource(Res.string.assistant_still_applies_title),
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		Text(
			text = component.statement,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		AssistantStillAppliesButtonsView(
			memoryId = component.memoryId,
			onAnswer = { applies ->
				onAction(AssistantChat.Action.AnswerStillApplies(component.memoryId, applies))
			}
		)
	}
}
