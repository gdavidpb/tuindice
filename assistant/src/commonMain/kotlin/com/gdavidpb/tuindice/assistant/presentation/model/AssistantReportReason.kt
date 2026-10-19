package com.gdavidpb.tuindice.assistant.presentation.model

// The reasons the student picks when reporting a message. It mirrors the wire reason; the machine translates it.
enum class AssistantReportReason {
	INCORRECT,
	INAPPROPRIATE,
	HARMFUL,
	OTHER
}
