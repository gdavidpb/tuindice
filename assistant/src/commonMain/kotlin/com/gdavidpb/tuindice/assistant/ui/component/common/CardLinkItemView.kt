package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.CardLinkItem
import com.gdavidpb.tuindice.base.presentation.model.asString

// The link of a chat card, drawn from its item: a tap opens the screen the item names with the records it names.
@Composable
fun CardLinkItemView(
	link: CardLinkItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	CardLinkView(
		text = link.text.asString(),
		onClick = { onAction(AssistantChat.Action.OpenNavTarget(link.target, link.entityRefs)) },
		modifier = modifier,
		external = link.external,
		onClickLabel = link.accessibilityText?.asString()
	)
}
