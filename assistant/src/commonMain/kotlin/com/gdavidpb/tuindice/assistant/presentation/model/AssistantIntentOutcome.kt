package com.gdavidpb.tuindice.assistant.presentation.model

// How an intent ended, as the intent card shows it. It mirrors the wire outcome; the mapper translates between them.
enum class AssistantIntentOutcome {
	CONFIRMED,
	QUEUED,
	CONFLICT,
	REJECTED,

	// A refusal because the student's pensum is not loaded yet: the card says so, not the refusal's own label.
	PENSUM_NOT_LOADED,
	CANCELLED,
	EXPIRED,
	FAILED,
	DELEGATED
}
