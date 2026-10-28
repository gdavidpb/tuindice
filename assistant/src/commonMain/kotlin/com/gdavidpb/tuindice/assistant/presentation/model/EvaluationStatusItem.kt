package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.EvaluationTone
import com.gdavidpb.tuindice.base.presentation.model.UiText

// The state of an evaluation: its word, and the tone the word is drawn in, as the Evaluations tab draws the same state.
data class EvaluationStatusItem(
	val text: UiText,
	val tone: EvaluationTone
)
