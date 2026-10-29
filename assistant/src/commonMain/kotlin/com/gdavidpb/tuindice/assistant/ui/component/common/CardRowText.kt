package com.gdavidpb.tuindice.assistant.ui.component.common

// The text of a row of a chat card: its [name], in the muted tone, its [codes], drawn as the app draws subject
// codes, and its [title], a bold text that is not a code (the title of a warning or a plan). A text wraps onto as
// many lines as it needs and never ends in an ellipsis.
data class CardRowText(
	val name: String,
	val codes: List<String> = emptyList(),
	val title: String? = null
)
