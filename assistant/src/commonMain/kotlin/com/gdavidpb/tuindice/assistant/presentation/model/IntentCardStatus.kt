package com.gdavidpb.tuindice.assistant.presentation.model

// Where an intent card stands: waiting for the student, running, or resolved into a receipt with its outcome.
sealed interface IntentCardStatus {
	data object Open : IntentCardStatus
	data object Running : IntentCardStatus
	data class Resolved(val outcome: AssistantIntentOutcome) : IntentCardStatus
}
