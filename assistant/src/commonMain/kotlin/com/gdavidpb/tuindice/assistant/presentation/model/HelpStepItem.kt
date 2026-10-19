package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One step of the help: its number, and its text.
data class HelpStepItem(
	val numberText: UiText,
	val text: String
)
