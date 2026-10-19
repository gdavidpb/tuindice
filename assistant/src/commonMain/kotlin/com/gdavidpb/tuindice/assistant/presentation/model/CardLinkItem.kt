package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityRef

// The link of a chat card: its text, and the screen it opens with the records it names.
data class CardLinkItem(
	val target: NavTarget,
	val text: UiText,
	val entityRefs: List<EntityRef>
)
