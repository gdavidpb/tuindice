package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// The link that calls a phone of the support: its text, and the tel: address the device dials.
data class SupportCallItem(
	val text: UiText,
	val telUri: String
)
