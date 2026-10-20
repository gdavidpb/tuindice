package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.CardLinkItem
import com.gdavidpb.tuindice.base.presentation.model.asString

// The row of a chat card that is a link to a screen, drawn from the link of its item: a tap anywhere on the row
// opens the screen the link names with the records it names, and a screen reader says what that is. A row with no
// [link] is a plain row.
@Composable
fun CardLinkRowView(
	link: CardLinkItem?,
	onAction: (AssistantChat.Action) -> Unit,
	text: CardRowText,
	showsDivider: Boolean = false,
	trailing: (@Composable () -> Unit)? = null
) = CardRowLayout(
	text = text,
	showsDivider = showsDivider,
	trailing = trailing,
	onClick = link?.let { item -> { onAction(AssistantChat.Action.OpenNavTarget(item.target, item.entityRefs)) } },
	onClickLabel = link?.let { item -> (item.accessibilityText ?: item.text).asString() }
)
