package com.gdavidpb.tuindice.assistant.ui.component.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.OpenScreenItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkView

// nav.abrir/1: a link that opens its screen in the app, with the label the service gives.
@Composable
fun OpenScreenComponentView(
	item: OpenScreenItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	CardLinkView(
		text = item.label,
		onClick = { onAction(AssistantChat.Action.OpenNavTarget(item.target, item.entityRefs)) },
		modifier = modifier.testTag(AssistantUiTags.Component.openScreen(item.id))
	)
}
