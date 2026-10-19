package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One count of the record, with its label.
data class CountRowItem(
	val label: UiText,
	val value: String
)
