package com.gdavidpb.tuindice.assistant.presentation.model

// How an intent ended, as the intent card shows it. It mirrors the wire outcome; the mapper translates between them.
enum class AssistantIntentOutcome {
	CONFIRMED,
	QUEUED,
	CONFLICT,
	REJECTED,
	CANCELLED,
	EXPIRED,
	FAILED,
	DELEGATED
}
