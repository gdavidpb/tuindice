package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One subject: its code, its name with its credits, the chip of its status, and the link to its screen.
data class SubjectRowItem(
	val code: String,
	val nameText: UiText,
	val statusText: UiText?,
	val link: CardLinkItem
)
