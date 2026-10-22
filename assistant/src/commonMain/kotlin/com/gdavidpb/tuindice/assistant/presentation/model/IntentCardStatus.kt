package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// Where an intent card stands: waiting for the student, running, or resolved into a receipt with its outcome. A receipt
// that says what was added carries its [receipt]; otherwise the receipt reads the outcome.
sealed interface IntentCardStatus {
	data object Open : IntentCardStatus
	data object Running : IntentCardStatus
	data class Resolved(val outcome: AssistantIntentOutcome, val receipt: UiText? = null) : IntentCardStatus
}
