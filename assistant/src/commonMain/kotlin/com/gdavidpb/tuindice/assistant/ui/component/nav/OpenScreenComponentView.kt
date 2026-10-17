package com.gdavidpb.tuindice.assistant.ui.component.nav

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.OpenScreenComponent

// nav.abrir/1: a button that opens its screen in the app, with the label the service gives.
@Composable
fun OpenScreenComponentView(
	component: OpenScreenComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	OutlinedButton(
		onClick = { onAction(AssistantChat.Action.OpenNavTarget(component.target, component.entityRefs)) },
		modifier = modifier.testTag(AssistantUiTags.Component.openScreen(component.id))
	) {
		Text(text = component.label)
	}
}
