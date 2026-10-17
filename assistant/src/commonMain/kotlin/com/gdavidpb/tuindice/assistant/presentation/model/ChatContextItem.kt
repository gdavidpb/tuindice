package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext

// The chip at the top of the sheet: [label] says what the student is looking at («Evaluaciones · CI4325 Parcial 1»).
data class ChatContextItem(
	val label: String,
	val context: AssistantScreenContext
)
