package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityRef

// The link of a chat card: its text, and the screen it opens with the records it names. [accessibilityText] names the
// entity the link opens, for a screen reader, when the text alone repeats beside other links («Ver materia» for each
// subject); null reads the text. [external] draws the mark of a link that leaves the app.
data class CardLinkItem(
	val target: NavTarget,
	val text: UiText,
	val entityRefs: List<EntityRef>,
	val accessibilityText: UiText? = null,
	val external: Boolean = false
)
