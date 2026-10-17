package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.model.chatCardLink
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import org.jetbrains.compose.resources.stringResource

// The link at the foot of a chat card: it opens the screen of the record the card names, when [chatCardLink] finds one.
// A card that names no record with a screen draws nothing.
@Composable
fun CardRecordLinkView(
	entityRefs: List<EntityRef>,
	onAction: (AssistantChat.Action) -> Unit
) {
	val (target, label) = entityRefs.chatCardLink() ?: return

	CardLinkView(
		text = stringResource(label),
		onClick = { onAction(AssistantChat.Action.OpenNavTarget(target, entityRefs)) }
	)
}
