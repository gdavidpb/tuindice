package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat

// The sheet's body by state: the consent comes first, then the conversation. The closed states draw nothing.
@Composable
fun AssistantChatContent(
	state: AssistantChat.State,
	onAction: (AssistantChat.Action) -> Unit,
	isCameraAvailable: Boolean = false,
	modifier: Modifier = Modifier,
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
	when (state) {
		AssistantChat.State.Idle, AssistantChat.State.Unavailable -> Spacer(modifier = modifier)

		// The consent text may be taller than a medium sheet on a small screen, so it scrolls.
		AssistantChat.State.ConsentRequired, AssistantChat.State.ConsentFailed -> AssistantConsentView(
			failed = state == AssistantChat.State.ConsentFailed,
			onAction = onAction,
			modifier = modifier.verticalScroll(rememberScrollState())
		)

		is AssistantChat.State.Content -> AssistantChatConversationView(
			content = state,
			isCameraAvailable = isCameraAvailable,
			onAction = onAction,
			modifier = modifier,
			snackbarHostState = snackbarHostState
		)
	}
}
