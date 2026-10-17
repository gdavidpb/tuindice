package com.gdavidpb.tuindice.assistant.ui.component.sistema

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.view.AssistantConsentView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView

// sistema.consentimiento/1: the consent text of the sheet, embedded in the message.
@Composable
fun ConsentComponentView(
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier) {
		AssistantConsentView(onAction = onAction)
	}
}
