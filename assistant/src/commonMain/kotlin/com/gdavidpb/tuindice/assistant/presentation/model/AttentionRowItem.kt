package com.gdavidpb.tuindice.assistant.presentation.model

// One item that needs attention: what it is, its detail, and the link to the record it names, when its kind has a
// screen.
data class AttentionRowItem(
	val title: String,
	val detail: String,
	val link: CardLinkItem?
)
