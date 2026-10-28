package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// Where an intent card stands: waiting for the student, running, or resolved into a receipt with its outcome. A receipt
// that says what was added carries its [receipt]; otherwise the receipt reads the outcome. A card resolved with an
// outcome this build does not know is closed: it keeps what it proposed, with no actions and no outcome label.
sealed interface IntentCardStatus {
	data object Open : IntentCardStatus
	data object Running : IntentCardStatus
	data class Resolved(val outcome: AssistantIntentOutcome, val receipt: UiText? = null) : IntentCardStatus
	data object Closed : IntentCardStatus
}
